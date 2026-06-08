# PLANTPOTTING-0013 — Critique of CODEX and DEEPSEEK drafts (from the CLAUDE author)

Scope: I (CLAUDE draft) compare the two sibling drafts against mine on five axes — what's
stronger, what's weaker, missing tasks, underweighted risks, wrong sequencing — then say what
I'd merge. All three agree on the shape (Thread A conditional direct Pilea card behind an
elevated `per_species_thresholds` bar composed with the boundary gate; Thread B deepen the 8
single-photo species; doc cleanup of stale Pilea-deferral prose in `ml-mapping-notes.md`;
v0.7.0 + tag). The disagreements are in method rigour and in which invariant is load-bearing.

---

## Draft CODEX (`PLANTPOTTING-0013-CODEX.md`)

### Stronger than mine
- **Offline threshold-simulation script (Phase 3, "Add or update an offline analysis script…
  that simulates candidate Pilea thresholds and margins from the CSV without changing app
  code" + `pilea-direct-card-threshold-sweep.csv`).** This is the best single idea in any of
  the three drafts. My Phase A1 re-derives the two confidence distributions and a LOO fold
  table *in prose* off the pulled CSV, but I never formalise a reusable simulator. CODEX's
  sweep CSV (fold id, tuning photos, held-out photo, threshold, margin bar, route, correctness,
  confident-wrong, pothos-sentinel outcome) is a concrete, auditable artifact and — critically —
  it decouples threshold iteration from the slow, finicky GMD device-CSV-pull cycle. Given the
  known pain of the device-eval CSV pull (wipe-on-reinstall, Git-Bash path mangling), iterating
  on the device is exactly what you want to avoid. This is materially better than my approach.
- **Pre-committed threshold *grid* before validation (Phase 2, "Pre-commit the candidate
  threshold grid before validation").** This is stricter anti-overfit discipline than my "derive
  `T_pilea` from the other 5 per fold." Committing the full candidate set *before* seeing any
  fold prevents the LOO selection step itself from leaking. My Phase A1 is honest about overfit
  but does the grid implicitly; CODEX makes it an explicit pre-registration task. Better.
- **Phase 1 makes "read the 0012 evidence" a task** (`boundary-gating-decision.md`,
  `post-pilea-gated-summary.md`, `feedback.md`). My draft references those files in the anchors
  but never turns "read them first" into a checkbox. CODEX's Phase 1 is a cleaner cold-start.

### Weaker than mine
- **Margin treated as a real separator bar — and the math says it can't be.** CODEX Phase 2/3
  require Pilea to "clear a Pilea-specific top1-top2 margin bar" as a co-equal gate. My Phase A0
  / A2 ("Rejected — margin-over-second as the separator") proves this is futile: a real Pilea
  (~0.98, no second-place mass) and a pothos-misread-as-Pilea (0.9661, no second-place mass)
  *both* have enormous margins. Margin cannot tell them apart; the **only** separating lever is
  the absolute Pilea top-1 value. CODEX never confronts this, so it risks tuning a margin bar
  that does nothing — or, worse, banks false safety on it. This is CODEX's biggest analytical
  gap.
- **No config-level CI bind that `per_species_thresholds["pilea-peperomioides"] > 0.9661`.**
  CODEX Phase 5 adds the 0.9661 sentinel *unit test* ("`pothosToPileaSentinelNeverReturnsDirect
  PileaCard`"), which is good, but it never binds the *config value itself* to sit above the
  documented pothos ceiling. My `ModelManifestTest` extension ("if `per_species_thresholds`
  contains Pilea, its value is > 0.9661") is a second, independent CI tripwire at the manifest
  layer. CODEX can pass its sentinel test with a synthetic row while still shipping a manifest
  threshold set too low, because nothing CI-binds the manifest number.
- **No "separation gap sign → early abort" rule.** My Phase A1 says: if (min real-Pilea top-1) −
  (max pothos→Pilea top-1) ≤ 0, no safe `T_pilea` exists → fall back immediately. CODEX has a
  fallback (Phase 2/3) but states it as "if no threshold/margin pair passes" — it never makes
  the gap-sign the crisp, up-front go/no-go. Weaker decision hygiene.

### Missing tasks
- The margin-can't-separate analysis (my A0/A2). Should be added as a "Rejected mechanism" note
  so an executor doesn't waste a phase tuning a dead lever.
- The config-level `> 0.9661` manifest invariant (my Phase A3 `ModelManifestTest` extension).
- An A0-style "state the central problem honestly" framing — CODEX is procedurally thorough but
  never names the crux (two failure modes near-identical in score shape).

### Underweighted risks
- **The n=1 pothos problem.** CODEX's risks list ("A high Pilea score alone still admits the
  known pothos→Pilea failure") treats pothos→Pilea as a population, but never asks how many
  pothos fixtures actually mispredict as Pilea. DEEPSEEK nails this (see below) — you may be
  fitting `T_pilea` above a *single* data point, which is far more overfit-prone than the draft
  implies. CODEX is silent on it.

### Wrong sequencing
- **10 strictly-numbered phases serialise Thread B (Phase 7) entirely after Thread A (Phases
  1–6).** The two threads are independent (different files, different gates); my draft and
  DEEPSEEK's both parallelise them. CODEX's own Sequencing section only requires "fixture
  sourcing before the final scorecard," so the serial phase numbering is stricter than the real
  dependency and wastes the parallelism. Minor, but it's the wrong default for an executor
  reading top-to-bottom.

---

## Draft DEEPSEEK (`PLANTPOTTING-0013-DEEPSEEK.md`)

### Stronger than mine
- **Concrete repo reconnaissance.** DEEPSEEK's anchors table cites `ModelScoreMapper.kt` line
  69–74 (the `plainThreshold` lookup), the `perSpeciesThresholds` origin ("added in 0005 §5.3"),
  and the existing `_comment_per_species_thresholds` 0006-discipline comment. My anchors give a
  line *range* (~89–103) but DEEPSEEK's are sharper and show it actually traced the mechanism it
  intends to reuse. A4 ("the new comment supplements" the existing 0006 comment) is a nice touch
  I missed.
- **Named author-separation, ready to execute.** A2 lists the six Pilea fixture authors by name
  (dinomariobob, Tiago Lubiana, Olsza Borys, Daniel Atha, dmagdee, Curran Dwyer). My Phase A1
  says "use the `author` column" but stops there. DEEPSEEK turning the actual authors into the
  task makes the LOO author-separation immediately actionable and lets a reviewer sanity-check
  that the six folds are genuinely author-distinct.
- **The empirical per-pothos-fixture prediction breakdown (Risk #3).** DEEPSEEK records that
  `epipremnum-aureum__01 → pothos, __02 → pothos, __03 → alocasia, __04 → calathea, __05 →
  monstera, __06 → alocasia` — i.e. **only the base `epipremnum-aureum.jpg` mispredicts as
  Pilea.** This reframes the entire safety calculus: the dangerous pothos→Pilea error may be a
  *single-fixture* phenomenon, which means `T_pilea` is being fit above **one** point. My draft
  treats "max pothos→Pilea" as a distribution ceiling without surfacing that the distribution
  might be n=1. This is the sharpest empirical read in any draft.
- **Honest about CI un-enforceability (A8).** DEEPSEEK states plainly that the "0 pothos→direct
  Pilea card" property lives in an on-device eval and "If this is unenforceable in CI, document
  it as a manual review gate." That's more honest than hand-waving a CI bind. (My draft *does*
  add a real CI bind — but only at the config/synthetic-unit layer; DEEPSEEK is right that the
  eval-level property itself can't be CI-bound, and says so.)
- **Quantifies the eval surface** ("all 7 pothos fixtures × 11 perturbations each = 77 pothos
  rows"). Makes the +0-confident-wrong bar concrete in a way my prose doesn't.

### Weaker than mine
- **A7(c) leaves the most important invariant fuzzy.** On the live 0.9661 case the test note
  reads: "if above → this IS the confident-wrong window the eval must catch; the unit test can
  encode the pre-commit constraint." It never commits to `T_pilea > 0.9661` as a hard, CI-bound
  *config* invariant. My Phase A3 makes it non-negotiable: a `ModelManifestTest` that fails CI if
  the shipped Pilea threshold is ≤ the documented pothos ceiling. DEEPSEEK identifies the danger
  but stops short of the tripwire that closes it.
- **No "state the crux / gap-sign abort" method front-matter.** DEEPSEEK jumps straight to LOO
  tuning (A2) assuming a safe threshold exists. Ironically, its own Risk #3 (pothos→Pilea may be
  n=1) is exactly the input to my Phase A1 stopping rule (if min-real-Pilea ≤ max-pothos→Pilea,
  abort) — but DEEPSEEK never closes that loop into the method. It surfaces the insight as a risk
  and then doesn't let it gate the tuning.
- **No offline simulation script** (CODEX's strength). DEEPSEEK still tunes off the pulled CSV
  (A2) like I do, but doesn't formalise a reusable, device-independent sweep. Both of us are
  weaker than CODEX here.

### Missing tasks
- The config-level `> 0.9661` manifest CI bind (mine; CODEX also lacks it).
- The offline threshold-sweep script (CODEX's; DEEPSEEK lacks it).
- An explicit gap-sign early-abort decision (mine).

### Underweighted risks
- DEEPSEEK *finds* the n=1 pothos risk but **underweights it in the method**: it should drive a
  "we are fitting above a single pothos point, so the fail-safe is load-bearing and the bar is
  conservative" stance in the tuning phase, not just appear in the risk list. The risk is
  correctly identified and then under-used.
- **Schema-test drift.** Neither DEEPSEEK nor I flag that growing the fixture set could force an
  `AccuracyEvalCsvSchemaTest` update — CODEX's Phase 8 does ("Update or add the schema guard…
  without dropping required columns"). That's a CODEX strength both of us miss.

### Wrong sequencing
- DEEPSEEK's sequencing is essentially correct and close to mine (Phase 1 A1–A6 sequential; B
  and C parallel; ship gates on all). One soft spot: A4 (set manifest threshold) and A3 (code)
  are correctly ordered, and the B4 re-run note ("use whichever gate configuration wins") is
  right. No real sequencing error — this is the best-sequenced of the two siblings.

---

## If I were merging

**Keep from CODEX:**
1. **The offline threshold-simulation script + `pilea-direct-card-threshold-sweep.csv`**
   (Phase 3) — decouple LOO tuning from the device cycle. This replaces my prose-only Phase A1
   fold table and is the highest-value import.
2. **The pre-committed threshold *grid*** (Phase 2) — register candidates before seeing folds,
   stricter than my per-fold derivation.
3. **The "read 0012 evidence first" Phase 1 task** and the **`AccuracyEvalCsvSchemaTest`
   schema-drift guard** (Phase 8) — both close real gaps in mine and DEEPSEEK's.

**Keep from DEEPSEEK:**
1. **The named-author LOO author-separation** (A2, the six authors) — makes my "use the author
   column" immediately executable and reviewer-checkable.
2. **The empirical per-pothos-fixture prediction breakdown** (Risk #3, "only `epipremnum-aureum`
   base mispredicts as Pilea") — and promote it from a risk into a *method input*: the n=1
   framing should set the conservatism of `T_pilea` and reinforce that the fail-safe is
   load-bearing.
3. **The line-precise anchors** (`ModelScoreMapper.kt` 69–74, `perSpeciesThresholds` 0005 §5.3,
   the 0006 `_comment` to supplement) and the **A8 honesty** that the eval-level invariant is a
   manual/review gate, not CI.

**Retain from mine (CLAUDE) — neither sibling has these and they're load-bearing:**
1. **Phase A0 + the "margin cannot separate, absolute top-1 is the only lever" crux** — this
   kills CODEX's futile margin bar before an executor spends a phase on it.
2. **The config-level CI bind `per_species_thresholds["pilea-peperomioides"] > 0.9661`** in
   `ModelManifestTest` — the one tripwire that makes "a pothos→Pilea input can never reach a
   direct card" enforceable below the on-device eval. DEEPSEEK's A8 correctly says the eval
   itself can't be CI-bound; this is the compensating control that *can*.
3. **The separation-gap-sign early-abort rule** (if min-real-Pilea top-1 ≤ max-pothos→Pilea
   top-1, no safe threshold exists → fall back before tuning) — the crisp go/no-go both siblings
   soften into a generic "if nothing passes" fallback.

Net: the merged plan is CODEX's tuning *rigour* (offline sweep + pre-committed grid + schema
guard) running on DEEPSEEK's *empirical grounding* (named authors, n=1 pothos reality, precise
anchors), gated by my two non-negotiable invariants (margin-is-not-the-separator; manifest
threshold CI-bound > 0.9661 with a gap-sign abort). The conditional-ship-or-fall-back spine and
Thread B / doc-cleanup / v0.7.0+tag scope are identical across all three and carry over as-is.
