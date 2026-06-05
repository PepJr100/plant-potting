# PLANTPOTTING-0008 — CLAUDE critique of CODEX and GEMINI drafts

Critique author: CLAUDE (draft: `PLANTPOTTING-0008-CLAUDE.md`). Scope under review: the
training-data availability spike (6 OOV species + pothos/Pilea boundary, assess-only) plus the
shutter-on-return UX fix.

---

## CODEX (`PLANTPOTTING-0008-CODEX.md`)

### What is stronger than mine

- **Split-by-observation/author de-dup discipline (Phase 5).** CODEX's task "mark whether enough
  unique authors/observations appear to support disjoint train/val/test splits **by observation or
  author rather than by image crop**" is the single best idea in any of the three drafts. My draft's
  "Disjoint-split feasibility per species" only guards disjointness *against the 8 fixtures* — it
  silently assumes within-source images are independent. They are not: iNat observations carry
  multiple near-duplicate photos by one author, and crop-level splitting leaks train into test. CODEX
  catches a real ML-correctness failure mode I underweighted. This belongs in the merge.
- **iNat photo-license vs observation-license distinction (Phase 2).** CODEX's "explain whether
  observation-level and photo-level licenses both need checking" is correct and I got it slightly
  wrong: my draft calls it a "per-observation CC license," but iNaturalist licenses are set
  **per-photo**, independent of the observation. CODEX's framing prevents counting a CC-BY observation
  whose individual photos are CC-BY-NC.
- **Preventive image-commit guard (Phase 1).** CODEX adds a local `.gitignore` under the evidence dir
  ignoring `*.jpg/*.jpeg/*.png/*.webp/downloads/samples/raw/` + cache. Mine relies only on the Phase 4
  `git status` check — a *detective* control after the fact. CODEX's `.gitignore` is a *preventive*
  control and strictly better; both together is best.
- **`count_confidence` column + per-image-license availability downgrade (Phases 4–5).** CODEX records
  a high/medium/low confidence per count and downgrades where per-image license can't be verified.
  My report columns capture license type and pitfalls but never a confidence grade — so a thin,
  shaky 60 and a solid 60 look identical against the floor. CODEX's grading is a real improvement.
- **`source-counts.csv` machine-readable artifact (Phase 1 / acceptance).** A diffable CSV alongside
  the prose report. My `go-no-go-matrix.md` is prose-table only; the CSV is easier to validate and
  re-run.
- **Exact existing-test method names in the camera fix (Phase 7).** CODEX names
  `CameraScreenTest.failureRetryReturnsToIdle`, `CameraViewModelTest.newCaptureClearsFailureState`,
  `CameraScreenBindStateTest.shutterIsDisabledAndBindProgressVisibleWhileImageCaptureIsNull`, and
  `…BoundStateTest.shutterIsEnabledAndBindProgressGoneWhenImageCaptureIsBound`. My draft lists the
  test *classes* but not the methods — CODEX is more precise about the exact regression surface.
- **Per-species×per-source enumeration (Phase 4).** 28 explicit `- [ ]` boxes (7 targets × 4 sources)
  make it impossible for an executor to silently skip a species or a source. My "one pass per target
  × per source" prose is more compact but easier to under-deliver.

### What is weaker than mine

- **The camera fix is investigatory, not diagnosed (Phase 7).** CODEX's first two tasks are
  "Reproduce the bug…" and "Inspect `CameraScreen.kt` and `CameraViewModel.kt` to find **whether**
  stale `Success`/`Capturing`/`Identifying` state is preventing the shutter…" — i.e. it asks the
  executor to *discover* the root cause. My draft already pins it: shutter gate at
  `CameraScreen.kt:191` (`(Idle||Failure) && imageCapture != null`), state advance to `Success` at
  `CameraViewModel.kt:51`, and the existing `reset()` at `CameraViewModel.kt:61` as the fix lever. A
  pre-diagnosed plan is materially cheaper to execute and review.
- **No aggregate GO decision rule.** CODEX's "Write the overall GO/NO-GO gate… including which species
  block a full fine-tuning sprint if any" never states the *rule* — what fraction of targets must
  clear the floor for an overall GO. My draft commits to "GO only if a useful majority clear the floor
  AND the pothos/Pilea pair is sourceable." Without a rule, the overall verdict is hand-wavy.
- **Gate ordering is not cheap→expensive (Phase 8).** CODEX runs focused unit → **instrumented** →
  verifyNoNetworking → stub → full unit → **ktlint last**. The cheapest gate (ktlint, seconds) runs
  after the most expensive (GMD instrumented tests, minutes). Mine orders ktlint → JVM → net-free →
  stub → instrumented → integration, so a lint failure fails fast.
- **Missing `scripts/integration-flow.ps1` gate.** My Phase 4 runs `integration-flow.ps1`
  cold/warm/buildonly to confirm no artifact re-baseline. CODEX's Phase 8 omits it entirely — a gate
  gap given this repo's established integration-flow discipline.
- **Verbosity / executor fatigue.** Phase 4's 28 near-identical bullets (and Phase 6's 6 near-identical
  per-species recommendation bullets) are a wall of boilerplate that would read better as a single
  table + a "fill one row per species/source" instruction — which is exactly what `source-counts.csv`
  already is. The enumeration is good for completeness but should be expressed once, not thrice
  (Phase 3 setup, Phase 4 queries, Phase 6 recommendations all re-list the same 8 targets).

### Tasks missing (vs mine)

- No `integration-flow.ps1` gate.
- No explicit retrieval-date stamp on counts as a snapshot (my R9 / methodology note); CODEX's
  `query-log.md` records "dates queried" but never frames counts as floor-as-of-date.
- No JVM-layer assertion that the camera fix must **not** fire mid-flight stated as its own guard
  task — it's folded into one Phase 7 bullet ("preserving … in-flight capture protection") rather
  than a dedicated test like my `CameraViewModelTest` `Success → reset() → Idle` case plus the
  explicit `Capturing`/`Identifying` guard.

### Risks underweighted

- **No API rate-limit risk.** GEMINI flags this and CODEX does not. Phase 4's 28 live queries across
  four sources will hit iNat/GBIF/Flickr throttling; CODEX has no throttle/manual-UI mitigation.
- **Scope-creep-into-model-work has strong non-goals but no risk bullet.** My R7 ("spike inflates
  into model work") makes the failure mode explicit; CODEX relies on the non-goals list alone.

### Sequencing wrong

- **The independent code fix is over-serialized.** CODEX's "Sequencing" says implement Phase 7
  "independently of the research work, but **land it in the same sprint only after confirming** it
  does not touch model/KB/networking," then Phase 8 "after both … are complete." The camera fix shares
  *no files* with the research and could land first or fully in parallel; CODEX's eight-phase spine
  reads as strictly serial 1→8 and under-exploits the parallelism my draft calls out explicitly.

---

## GEMINI (`PLANTPOTTING-0008-GEMINI.md`)

### What is stronger than mine

- **API rate-limit risk + mitigation.** GEMINI's "Automated API scripts might violate rate limits →
  use manual UI searches or low-volume throttled requests" is a genuine operational risk that **both
  my draft and CODEX omit**. Worth lifting verbatim.
- **Brevity / readability.** GEMINI is the only draft you can skim in one screen. My draft (and
  CODEX's) are thorough but heavy; GEMINI is a useful reminder that the executor has to actually read
  this. Its three-phase spine is the right *shape* even if the content is too thin.

### What is weaker than mine

- **The camera fix is the weakest of all three (Phase 3).** Four generic bullets: "Locate the shutter
  button state… Identify why… Implement the fix… Write/update a UI test." No root cause, no file
  paths, no `reset()` lever, no guard against resetting mid-`Capturing`/`Identifying`, no JVM unit
  test, and no named existing tests to keep green. Mine pins `CameraScreen.kt:191` /
  `CameraViewModel.kt:51,61`; CODEX at least names the regression tests. GEMINI gives the executor
  nothing.
- **Collapses three sources into one task.** Phase 1's "Repeat the above queries (all 6 OOV species +
  pothos/Pilea) across GBIF, Wikimedia Commons, and Flickr-CC" hides ~21 sub-queries behind a single
  checkbox — trivially under-delivered, and with no per-source license-breakdown requirement.
- **Conflates evidence with results.** GEMINI writes the GO/NO-GO and the fine-tuning outline directly
  into `docs/sprints/results/PLANTPOTTING-0008.md`. My draft (and CODEX's) keep the report, matrix,
  and `finetune-sprint-outline.md` under `evidence/` and reserve the results doc for the verdict +
  gate summary. Putting the outline in the results doc muddies the sprint-close artifact.
- **No fixture exclusion detail.** GEMINI says splits must avoid "the 8 existing fixture images" but
  never lists them, and — critically — **misses that `epipremnum-aureum.jpg` overlaps the pothos
  boundary target** and its source URL must be excluded from any pothos training count. Both my draft
  and CODEX call this out; GEMINI's omission would let the existing pothos fixture leak into the
  pothos count.
- **No methodology / controlled-vocabulary setup.** No species-targets file, no synonym/cultivar
  vocabulary fixed *before* querying, no retrieval-date stamp. CODEX (Phase 3) and I both front-load
  this; without it, GEMINI's counts are queried against ad-hoc search terms.
- **Weak gate coverage.** GEMINI has no gate phase — acceptance lists `verifyNoNetworking` and stub
  isolation but never `ktlintCheck`, `testDebugUnitTest`, the instrumented camera run, the
  `git status` no-binaries check, or `integration-flow.ps1`.

### Tasks missing (vs mine)

- Controlled-vocabulary / `species-targets.md` setup; methodology note + retrieval-date stamp.
- Per-source license breakdown (CC0/BY/BY-SA usable vs BY-NC/ND excluded).
- Cross-source de-dup (iNat resurfacing via GBIF).
- Fixture enumeration and the `epipremnum-aureum.jpg` pothos-count exclusion.
- A preventive image-commit guard (`.gitignore`) **and** the `git status` binary check.
- JVM `CameraViewModelTest` case; explicit ktlint / testDebugUnitTest / integration-flow gates.

### Risks underweighted

- **CC-BY-NC unusability.** GEMINI never flags that most iNat houseplant photos are CC-BY-NC —
  unusable for a commercial-app model — which inflates raw counts. This is my R1 and CODEX's Phase 2
  license rule; it is the central counting trap of the whole spike and GEMINI omits it.
- **Cross-source double-counting** (iNat ↔ GBIF). Not mentioned.
- **Pink Princess cultivar.** Handled only as a parenthetical "(noting cultivar availability vs
  generic)." My R2 and CODEX's Phase 5 base the recommendation **only on cultivar-proven counts** and
  route a sub-floor result to a standalone NO-GO. GEMINI leaves the highest-risk target soft.
- **Camera reset firing mid-flight / clobbering the failure banner.** Not addressed at all.

### Sequencing wrong

- The three-phase order itself is fine, and GEMINI correctly notes Phase 3 (UI fix) is independent and
  "can be picked up at any time." The real defect is the **absence of a final gate phase** and any
  cheap→expensive gate ordering — verification is implied only by acceptance criteria, not sequenced
  as runnable steps.

---

## If I were merging

- **Keep from CODEX:** (1) the **split-by-observation/author de-dup rule** from Phase 5 — it closes a
  near-duplicate train/test leakage hole my draft missed; (2) the **iNat photo-vs-observation license
  distinction** (Phase 2), which also corrects my imprecise "per-observation license" wording; (3) the
  evidence-dir **`.gitignore` preventive guard** (Phase 1) layered on top of my `git status` check;
  (4) the **`count_confidence` column + `source-counts.csv`** machine-readable artifact; and (5) the
  **exact existing-test method names** in Phase 7. I would **not** keep CODEX's investigatory camera
  approach, its ktlint-last gate ordering, or its triple re-listing of the 8 targets.
- **Keep from GEMINI:** (1) the **API rate-limit risk + throttle/manual-UI mitigation**, which neither
  CODEX nor I had; and (2) its **brevity as an editing target** — collapse my and CODEX's repeated
  per-species enumerations into the single `source-counts.csv` grid so the plan reads in one pass.
- **Keep from mine as the spine:** the pre-diagnosed camera root cause (`CameraScreen.kt:191`,
  `CameraViewModel.kt:51/61` → `reset()`), the explicit overall GO decision rule, the
  evidence/results artifact separation, the `epipremnum-aureum.jpg` pothos-count exclusion, the
  cheap→expensive gate phase including `integration-flow.ps1`, and the explicit research∥code
  parallelism.
