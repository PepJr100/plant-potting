# PLANTPOTTING-0011 — CLAUDE's critique of the CODEX and GEMINI drafts

> Reviewer: CLAUDE draft author. Comparing `PLANTPOTTING-0011-CODEX.md` and
> `PLANTPOTTING-0011-GEMINI.md` against my own draft (`PLANTPOTTING-0011-CLAUDE.md`) and the shared
> `_CONTEXT-0011.md`. Focus per instructions: the central methodological risk (over-fitting confidence
> thresholds to a tiny clean CC fixture set; honest accuracy measurement under the no-self-shot ban) and
> whether multi-crop/TTA latency is justified.

---

## Draft A — CODEX

### Stronger than mine

- **Multi-photo-per-species fixtures.** CODEX's Phase 1 task *"Add only CC0/public-domain real-photo JPEG
  fixtures … using `<kb-species-id>__NN.jpg` or an equivalent naming convention that supports multiple
  photos per species"* is materially better than my Phase-1a `<kb-species-id>.jpg` (one photo per
  species). More base photos per species is the *single most effective* defence against the central
  overfit risk — it's the only honest way to grow effective sample count under the no-self-shot ban,
  whereas perturbations of one photo do not. My draft missed this; it should be adopted.
- **Per-base-image-averaged metric (the sharpest anti-overfit point in any draft).** CODEX's risk
  *"Perturbations can overweight one source photo. Mitigation: aggregate both 'all variants' and 'per base
  image averaged' metrics so one heavily perturbed photo cannot dominate the sprint headline."* directly
  attacks a failure mode I under-specified: with a tiny clean set, one photo × N perturbations silently
  dominates the aggregate. My draft separates clean vs perturbation rows but never de-weights per-base.
  This belongs in the merged plan.
- **Characterization-before-change discipline.** CODEX Phase 3's first task — *"Add unit coverage around
  `ModelScoreMapper` for the existing direct high-confidence path, existing margin path, low-confidence
  path, unmapped confident top label path, and per-species threshold override behavior **before** changing
  policy"* — pins current behaviour before touching the gate. My 2a adds tests for the *new* gate but
  assumes the existing paths are already locked down. CODEX's ordering is safer.
- **CSV schema guard.** CODEX Phase 2's *"Add a small parser/check test for the generated CSV schema …
  so future evidence files cannot silently drop the confident-wrong or margin columns"* protects the
  evidence artifact itself. I have no equivalent.
- **Explicit preprocessing "control."** CODEX Phase 4's *"Add an internal preprocessing strategy
  abstraction … starting with the current full-frame square resize as the control"* makes the A/B
  baseline a first-class object. Mine implies the control but doesn't name it.
- **In-vocab-only metrics + KB-resolution integrity.** CODEX Phase 2 reports *"in-vocab-only metrics"* and
  Phase 1 adds a fixture integrity test that *"verifies the expected species id resolves in the KB and
  marks whether the species is reachable by the active model mapping."* With only 38/47 mapped, splitting
  in-vocab from out-of-vocab is the correct denominator; my draft folds everything into one accuracy
  number.
- **Stricter license rule (and mine is actually off-constraint here).** CODEX's manifest cross-check
  *"fails when a bundled identify fixture … declares anything other than CC0/public-domain."* That is
  exactly what `_CONTEXT-0011.md` mandates ("All real photos added must be CC0 / public-domain"). My
  draft's Phase-1a parenthetical — *"CC-BY-SA is allowed for test-only fixtures … but prefer CC0/PD"* — is
  a **constraint violation**; CODEX (and GEMINI) are correctly stricter. Drop my loosening.

### Weaker than mine

- **No AIY baseline anchor anywhere.** CODEX never mentions the AIY pinned baseline. My 1c and non-goals
  keep the AIY anchor (Monstera 0.8984 high-conf / jade low-conf) verbatim precisely so the harness
  plumbing and the new abstention gate can't *silently drift* the score path. Without it, a refactor of
  `ModelScoreMapper.map` or the preprocessor can change behaviour with nothing failing. This is a real
  hole given CODEX edits both.
- **Abstention field not specified as backward-compatible.** CODEX Phase 3 says *"add an above-plain
  minimum top1-top2 margin abstention"* and *"Implement the selected policy in `ModelScoreMapper`,
  `ModelManifest.Thresholds`, and … `model_manifest.json`"* — but never says the new field defaults to
  disabled / leaves AIY byte-for-byte unchanged, and there's no no-op-when-zero regression test. My 2a
  pins `highConfidenceAbstainMargin` default `0f` + a regression case proving identical behaviour at the
  default. As written, CODEX risks changing the AIY manifest's effective gating.
- **Anti-overfit is reporting-only, not a held-out tuning discipline.** CODEX *reports* base-photo vs
  perturbation metrics separately and *"require[s] multiple independent fixtures before per-species
  overrides"* — good, but it still **tunes the threshold against the same rows it then reports.** My 2b
  goes further: tune on a subset, then report confident-wrong on the **perturbation rows the gate was not
  tuned against** plus a **leave-one-species-out** split as the honest generalisation number. CODEX has no
  held-out set; separate-reporting ≠ held-out evaluation.
- **No Phase 0 baseline-green capture.** CODEX's verification is all at the end (Phase 6). My Phase 0
  records the pre-sprint green state so every later "GREEN throughout" claim has a documented baseline.
- **Re-tune loop not closed (see sequencing).**

### Missing tasks

- AIY baseline anchor preservation / drift guard.
- Default-disabled manifest field + no-op regression test for the new abstention lever.
- A held-out (perturbation-only / leave-one-species-out) generalisation number, distinct from the rows
  tuned against.
- Phase-0 pre-sprint green snapshot.

### Sequencing wrong

CODEX Phase 4 *"should branch from the post-abstention state so preprocessing is judged against the policy
users would actually see."* This is half-right and leaves a circularity open: if Phase 4 adopts
center-crop or TTA, the **raw score distribution changes**, so the thresholds tuned in Phase 3 against the
*squash* pipeline are now stale — yet CODEX never re-tunes. Whichever of "tune then change inputs under
it" you pick, you must close the loop. My draft's sequencing rule 3 explicitly re-runs Phase-2 tuning on
top of any adopted Phase-3 change. (Note: this is really a *shared* weakness — see GEMINI below and the
cross-cutting note at the end.)

---

## Draft B — GEMINI

### Stronger than mine

- **Concision and directness.** GEMINI is the tightest read, and its Phase 2 names the actual culprit —
  *"Seed `per_species_thresholds` … for specific chronic offenders (like snake plant)"* — tying the plan
  to the principal's reported failure. Useful as motivation. (But see the overfit caveat below: naming one
  anecdotal species as a per-species-seed target is double-edged.)
- That's essentially the extent of it. GEMINI is the thinnest of the three on everything else.

### Weaker than mine

- **★ Its central-risk mitigation is methodologically wrong.** GEMINI's headline overfit mitigation:
  *"The synthetic perturbations … effectively multiply the evaluation set size and simulate
  out-of-distribution variance, making the evaluation harness harder to overfit."* This is **backwards.**
  Perturbations of the same handful of base photos are *not* independent samples — they share content,
  lighting, framing, and species exemplar. They do **not** multiply effective sample size for
  generalisation, and tuning thresholds against them still overfits to those few base images. This is the
  exact trap `_CONTEXT-0011.md` flags as "the central methodological risk," and GEMINI's plan actively
  misreads it. CODEX (per-base averaging) and my draft (held-out perturbation rows framed as
  *synthetic-robustness, not real-world accuracy*) both get this right; GEMINI inverts it.
- **It both tunes on and "defends with" the perturbation set.** GEMINI's Phase 1 task extends the harness
  *"over the … perturbed fixture set"* and Phase 2 tunes against it, while the risk section claims that
  same perturbed set prevents overfit. You cannot tune against a set and simultaneously treat it as the
  held-out generalisation check. Confused.
- **Per-species seeding promoted to a primary lever.** GEMINI Phase 2 lists *"Seed `per_species_thresholds`
  … for chronic offenders"* as a co-equal task. Per-species seeds are the *most* overfit-prone lever; both
  my draft (2b: only behind an explicit evidence gate, prefer global) and CODEX (require multiple
  independent fixtures first) gate it hard. GEMINI invites the overfit it claims to avoid.
- **No AIY baseline anchor; no default-disabled abstention field; no no-op regression test.** Same gap as
  CODEX but with even less manifest/threshold detail. GEMINI's Phase 2 *"Modify `ModelScoreMapper` to
  support an additional margin-based abstention check"* says nothing about backward compatibility — direct
  risk to the frozen baseline.
- **No actionable verification phase.** GEMINI has no Phase-6-style gate task list (no explicit
  `testDebugUnitTest` / `verifyNoNetworking` / `check-stub-isolation.sh` / instrumented-compile commands);
  it relies on acceptance criteria to imply them. CODEX (Phase 6) and mine (Phase 5) make these runnable
  tasks.
- **No `ml-mapping-notes.md` calibration narrative.** GEMINI records numbers only in evidence `.md` files;
  it never updates `docs/kb/ml-mapping-notes.md` (the project's calibration-decisions home). Both other
  drafts do.
- **Fixture sourcing is hand-wavy.** Phase 1 *"Research and download an expanded set"* — no count target,
  no dimensions/encoding spec (480×480 / JPEG q80 to match existing fixtures), no naming convention, no
  scarcity-logging *task* (scarcity appears only in the risk list, so un-sourceable species can be
  silently dropped).
- **No Phase-0 baseline-green capture; no fixture KB-resolution integrity test; no CSV schema guard.**

### Missing tasks

- AIY anchor preservation; default-disabled abstention field + no-op regression test.
- Held-out / leave-one-species-out generalisation evaluation (it has the *opposite* — a wrong claim that
  perturbations solve overfit).
- Explicit verification-gate task list.
- `ml-mapping-notes.md` calibration update.
- Fixture dimension/encoding/naming spec; scarcity logging as a task; multiple-photos-per-species.

### Risks underweighted

- The overfit risk is not just underweighted — its mitigation is **incorrect** (above).
- TTA latency: GEMINI's only mitigation is *"If latency spikes beyond acceptable UX bounds, TTA will be
  discarded"* — no worst-case measurement, no median-of-N protocol, and crucially no comparison against
  the cheaper center-crop baseline (so it can't tell whether TTA buys anything center-crop didn't).

### Sequencing wrong

GEMINI: *"Phase 2 (Abstain) and Phase 3 (Try to Improve) can proceed in parallel or sequentially … both
must continuously validate against … Phase 1."* Running abstention tuning and preprocessing changes **in
parallel** is the worst option: preprocessing changes the score distribution the thresholds are tuned
against, so parallel work produces thresholds calibrated to a pipeline that no longer exists. No re-tune
is mentioned. CODEX at least serialises (Phase 4 from post-abstention state); my draft serialises *and*
re-tunes. GEMINI's parallelism is actively unsafe here.

---

## Multi-crop / TTA latency — is it justified? (all three)

All three correctly make TTA *evidence-gated and optional*, which is the right instinct: it's the
lowest-leverage of the three moves (the model is uncalibrated, so abstention is the real win), and it
multiplies interpreter runs on a camera path.

- **CODEX sets the right hurdle:** adopt TTA only if it beats **center-crop** (not just the squash
  control), measuring *"median and worst observed latency per image"* with the existing median-of-5
  protocol, *"otherwise leave the experiment documented but disabled."* That "improvement over center-crop"
  bar is the correct one — center-crop may capture most of the gain at ~1× cost, leaving TTA's 4–5× cost
  unjustified.
- **My draft** caps N ≤ 5, defaults off if marginal, and records per-identify latency — fine, but I
  compare TTA's top-1 delta against the *current* path generally rather than explicitly against
  center-crop. CODEX's framing is sharper; adopt it.
- **GEMINI** is weakest: discard "if latency spikes," no worst-case, no center-crop comparison.

Net: TTA is justified *only as a measured experiment that must clear the center-crop bar*, and should ship
default-off unless the number is decisive. CODEX expresses this best.

---

## Cross-cutting note (applies to all three, mine included)

Every draft sequences **MEASURE → ABSTAIN → IMPROVE**, which forces a threshold **re-tune** after any
preprocessing change (only my draft names the re-tune; CODEX/GEMINI leave it open or run it in parallel).
A cleaner order worth considering in the merge is **MEASURE → IMPROVE (preprocessing) → ABSTAIN**: lock
the raw pipeline first, then tune thresholds **once**, against the pipeline that actually ships. Whichever
order is chosen, the plan must state that thresholds are calibrated against the *final* preprocessing.

---

## If I were merging

**Keep from draft A (CODEX):**
- Multi-photo-per-species fixtures (`<kb-species-id>__NN.jpg`) + the **per-base-image-averaged** metric —
  the strongest honest-measurement levers under the no-self-shot ban.
- Characterization tests of the existing `ModelScoreMapper` paths *before* changing policy.
- In-vocab-only metrics, the fixture KB-resolution integrity test, the CSV-schema guard, and the named
  preprocessing "control."
- The TTA hurdle phrased as **"must beat center-crop"** with median + worst-case latency.
- Its strict CC0/PD-only cross-check (and **drop my CC-BY-SA loosening**, which is off-constraint).

**Keep from draft B (GEMINI):**
- Its brevity/clarity as the prose template, and the explicit motivation tying abstention tuning to the
  snake-plant failure — **but** demote per-species seeding to an evidence-gated last resort, and **delete
  its overfit-mitigation claim** (perturbations do not multiply sample size / cannot be both the tuning
  set and the generalisation check).

**Keep from my draft (CLAUDE):** the AIY baseline anchor preservation, the default-disabled
`highConfidenceAbstainMargin` field + no-op regression test, the **held-out perturbation /
leave-one-species-out** generalisation number, the "synthetic-robustness, not real-world accuracy" framing,
Phase-0 baseline-green capture, and the explicit re-tune-on-adopt loop — and add CODEX's per-base
averaging on top of it.
