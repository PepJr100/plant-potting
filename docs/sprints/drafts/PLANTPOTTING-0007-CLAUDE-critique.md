# PLANTPOTTING-0007 — CLAUDE critique of the CODEX and GEMINI drafts

Critique author: CLAUDE draft. Comparing the CODEX and GEMINI drafts against my own
(`PLANTPOTTING-0007-CLAUDE.md`). Citations are to task/goal/risk names in each draft.

---

## Draft A — CODEX (`PLANTPOTTING-0007-CODEX.md`)

CODEX is the near-twin of my draft: same six phases, same `ModelUnderTest` descriptor, same
candidate ids, same evidence paths. The differences are small but real.

### What's stronger than mine

- **Tighter, less self-referential prose.** CODEX's intro ("proved the wrong thing about the
  current engine") is a cleaner frame than my paragraph, and its non-goals are crisper. Where I
  pad ("the headline", "not a paper spike" repeated), CODEX states once and moves on.
- **Goal G4 phrasing is more honest about the seam default.** CODEX's wiring goal —
  "behind the existing `PlantIdentifier` / `IdentificationResult` seam **by default**, changing
  the seam only if the prototype proves a specific, documented need" — folds the constraint into
  the goal itself. Mine splits this across G4 and G6, which is more words for the same contract.
- **Phase 2 fixture-size task is more flexible.** CODEX's centre-crop task explicitly allows a
  different input size "if the candidate model requires a different input size that the
  preprocessor handles internally." My draft hard-pins 480×480/q80 and only relaxes it implicitly.
  CODEX's version is correct: PlantNet MobileNetV3 is natively 224×224, and the fixture-vs-model
  resize boundary matters.

### What's weaker than mine

- **No `IdSource.ON_DEVICE_MODEL` regression anchor as a standalone task.** CODEX's Phase 3 keeps
  the AIY baseline assertion (good), but its Phase 5 wiring lacks my explicit task to *re-confirm*
  `OnDevicePlantIdentifier` still returns `ON_DEVICE_MODEL` AND still routes weak predictions to
  low-confidence. CODEX has the route line but buries it; the source-tag assertion is softer.
- **No explicit sequencing diagram.** My ASCII dependency graph makes the Phase 1→shortlist and
  the "Phase 3 before Phase 5" gate visually unambiguous. CODEX's "Sequencing" is a prose list —
  same content, but the parallelism (Phase 2 ∥ candidate conversion) is easier to miss.
- **Missing the "keep AIY as the *default flag target*" subtlety.** My R8 and Phase 4 test task
  call out that AIY-bundle coverage tests must stay green *because AIY remains the flag default*.
  CODEX says "keep AIY as a baseline asset" but doesn't tie it to `ACTIVE_MODEL_ROOT`'s default,
  so an executor could delete AIY's manifest tests while "keeping the asset."
- **Acceptance criteria omit the size-measurement gate as its own line.** CODEX folds size into
  the harness-summary criterion; mine has a dedicated "Asset/APK size impact … measured and
  recorded (raw + compressed delta)" acceptance line. Size is a likely failure axis (R6/R1) and
  deserves its own checkbox.

### Missing tasks

- No task pinning the **AIY baseline numbers (Monstera ~0.8984, jade ~0.1055) as a regression
  assertion in the harness**. CODEX *asserts AIY rows keep "known behaviour"* (Phase 3) but never
  writes the actual numbers into the plan, so the executor has to rediscover them. Mine pins them.
- No `_REFRESH`/results-doc instruction to **flip the ledger only after all gates green** — CODEX's
  Phase 6 results task lists the contents but omits my "Flip the ledger entry only after every gate
  above is green" guard.

### Underweighted risks

- CODEX's risk list is missing my **R10 (GMD/probe flakiness / environment drift)** and **R11
  (sprint degrades into a paper spike)**. R11 is the whole reason G4 exists; without it as a named,
  acceptance-blocking risk, the live-smoke-run can quietly get dropped under time pressure. CODEX
  *does* keep the paper-spike acceptance criterion at the very end, so the gap is partial — but the
  risk register doesn't reinforce it.

### Sequencing

Essentially identical to mine and correct. No sequencing errors.

---

## Draft B — GEMINI (`PLANTPOTTING-0007-GEMINI.md`)

GEMINI is the lean draft: 5 phases, ~70 lines vs my ~330. It reads fast and is a good skeleton,
but the brevity drops several load-bearing constraints this project has learned the hard way.

### What's stronger than mine

- **Readability and altitude.** GEMINI's goals (G1–G5) and the "Sequencing & dependencies"
  numbered list are genuinely easier to scan than my wall of sub-bullets. For a human deciding
  "what is this sprint," GEMINI wins on first-read clarity.
- **Phase ordering puts fixtures first (Phase 1).** GEMINI opens with "Expanded Fixture Set" and
  frames it as "the ruler we measure candidates against" — a clean, correct articulation of why
  fixtures gate evaluation. My draft puts survey first; both orderings work, but GEMINI's "ruler"
  metaphor is the better justification and the fixtures genuinely *are* model-agnostic.
- **R4 names the overfit-the-choice risk plainly:** "Expanding the fixture set to 6+ species
  ensures we don't accidentally overfit our model *choice* to just Monstera and Jade." That's a
  sharper statement of selection bias than my R3 (which is about the KB16 model overfitting, a
  different thing). GEMINI caught a risk my draft underweights: choosing a winner on 2 fixtures.

### What's weaker than mine

- **No candidate matrix / decision-matrix artifact.** GEMINI's Phase 2 says "Survey at least 2–3
  candidate on-device models" but never produces `model-candidate-matrix.md` with
  redistribution-vs-dataset-public, conversion risk, input dtype, or size columns. This is the
  single biggest gap: 0006's lesson was that the *model* is the bottleneck, and GEMINI gives the
  executor no structured place to record *why* a candidate is or isn't viable before downloading it.
- **License-vs-weight-redistribution conflation.** GEMINI treats licensing as one axis ("Licensing
  is reported, not blocking"). My R1 and CODEX both separate **dataset-public ≠ weights-
  redistributable** — the exact trap with PlantNet-300K (public dataset, non-redistributable
  weights). GEMINI will let an executor assume "PlantNet is open" and check in weights it can't.
- **No machine-readable eval output.** GEMINI's Phase 3 says "Document the evaluation results" in
  prose. Mine and CODEX emit `model-swap-eval.csv` with a fixed column contract (raw top-1/3,
  mapped KB id, in-vocab flag, route, source, failure reason). Prose results aren't diffable across
  candidates or re-runnable; the CSV is the comparison substrate.
- **Phase 4 says "Create a new implementation of `PlantIdentifier`."** This is an architectural
  divergence and, I think, a mistake. The 0003–0006 infra already routes everything through one
  `OnDevicePlantIdentifier` + `OnDeviceIdentifyProvidersModule`; the swap is a *model-root switch*
  (`ACTIVE_MODEL_ROOT`), not a second identifier class. A new `PlantIdentifier` impl duplicates the
  interpreter/preprocessor/mapper wiring and invites stub-isolation leakage
  (`check-stub-isolation.sh`). My Phase 5 and CODEX's Phase 5 both correctly keep one identifier and
  thread the root through the existing module.
- **"Wire … behind a UI toggle" contradicts the no-new-screens non-goal.** GEMINI's Phase 4 lists
  "UI toggle" as a wiring option, but every draft (including GEMINI's own scope section, which bans
  "model picker") should keep this a `BuildConfig`/branch switch. "UI toggle" is user-facing chrome
  and conflicts with the project's no-net-new-screens posture.

### Missing tasks

- **No `check-stub-isolation.sh` gate.** GEMINI's Phase 5 runs only `verifyNoNetworking` and "all
  instrumentation tests." The stub-isolation guard is a known, named gate in this repo (mine Phase 6,
  CODEX Phase 6) and is *especially* relevant given GEMINI's new-`PlantIdentifier`-impl approach.
- **No `integration-flow.ps1` cold/warm/buildonly run** and **no expected-artifacts handling.** Both
  other drafts gate on it; GEMINI omits the integration transcript entirely, so a re-baseline of
  `expected-artifacts` could happen silently.
- **No `per_species_thresholds` honesty guard.** GEMINI's G5/Phase 4 says "Re-baseline the
  `perSpeciesThresholds` … based on the new model's confidence baseline" — but omits the
  non-negotiable "**never lower thresholds to bless ~10% predictions**" rule that 0006 established.
  This is the project's central routing-honesty discipline and GEMINI drops it.
- **No fixture-readability guard, no LICENSE.txt structural mirroring, no test-only/not-in-APK
  assertion.** GEMINI says "Append full provenance blocks" but doesn't require the fail-loud
  readability check or the "fixtures live under androidTest only" confirmation.
- **No per-JVM-test enumeration for re-baselining.** Mine and CODEX both list
  `ModelManifestTest`, `ModelAssetsPresenceTest`, `ModelLabelMappingValidationTest`,
  `PerSpeciesThresholdsContractTest`, `ModelScoreMapperPerSpeciesThresholdTest` (mine adds
  `ModelManifestDtypeContractTest`). GEMINI lists none, so the contract tests can rot silently.
- **No `ml-mapping-notes.md` content spec.** GEMINI mentions the file in Phase 5 but only for
  "licensing, size, and integration details" — not the alias decisions, coverage delta, or
  per-fixture outcomes that make that doc useful.

### Underweighted risks

- **No risk for the seam/wiring or AIY-deletion** equivalent to my R7/R8. Given GEMINI's
  new-impl approach, the "accidentally hardcode a candidate / delete AIY before comparison" risk is
  *higher* in GEMINI's plan, yet unlisted.
- **No networking-via-acquisition-script risk (my R9).** GEMINI bans production networking but never
  addresses the realistic vector: a model-download/convert script smuggling a network call into the
  build. That's the subtle way `verifyNoNetworking` actually gets breached.
- **Paper-spike risk is not named.** GEMINI has a "Live Prototype" goal (G4) and acceptance line,
  which is good, but no risk forcing the live-smoke-run to be acceptance-blocking the way my R11 does.

### Sequencing

GEMINI's high-level order (fixtures → survey/eval → prototype → verify) is correct. Two problems:
1. **Survey and evaluation are collapsed into one beat (Phase 2 & 3 "the core spike").** That hides
   the *shortlist gate* — survey should narrow to ≤2 candidates *before* fixture probing, or the
   harness ends up probing every candidate. My Phase 1 explicitly shortlists; GEMINI doesn't.
2. **Re-baselining (Phase 4) is placed *inside* "Prototype Integration", after wiring.** Mapping +
   thresholds should be settled *before/with* wiring so the app and harness share identical assets
   (my Phase 4 → Phase 5 ordering, CODEX likewise). GEMINI interleaves mapping updates with live-
   camera wiring in the same phase, risking app/harness asset drift.

---

## If I were merging

I'd keep **from CODEX**:
- Its tighter intro and the **G4 phrasing that bakes "seam unchanged by default" into the goal**.
- Its **flexible Phase 2 fixture-size task** (allow native model input size when the preprocessor
  handles the resize) instead of my hard 480×480 pin.
- Its crisper non-goals prose.

I'd keep **from GEMINI**:
- Its **fixtures-first framing and the "fixtures are the ruler" justification** (Phase 1).
- Its **R4 selection-bias risk** ("don't overfit the model *choice* to Monstera + Jade") — promote
  it to a named risk in the merged plan; my draft underweights it.
- Its **readability discipline**: lead each phase with a one-line "why this phase exists" before the
  task bullets, so the plan scans like GEMINI but carries my detail.

I'd keep **from my own (CLAUDE)** as the spine, because GEMINI drops too many project-specific
guardrails to use as the base:
- The **`model-candidate-matrix.md` decision matrix** with dataset-public ≠ weights-redistributable.
- The **`model-swap-eval.csv` machine-readable column contract** + pinned AIY baseline numbers.
- The **single `ACTIVE_MODEL_ROOT` switch** (NOT a second `PlantIdentifier` impl — reject GEMINI's
  Phase 4 approach) threading through `OnDeviceIdentifyProvidersModule`.
- The **threshold-honesty guard** ("never bless ~10% predictions"), **R9 acquisition-script
  networking risk**, **R11 paper-spike gate**, the **stub-isolation + integration-flow gates**, and
  the **enumerated JVM contract tests** to re-baseline.

Net: merged plan = my phase spine and guardrails, CODEX's goal wording and fixture-size flexibility,
GEMINI's per-phase "why" framing and its selection-bias risk.
