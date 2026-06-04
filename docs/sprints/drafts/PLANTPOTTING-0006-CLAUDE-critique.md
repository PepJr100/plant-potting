# PLANTPOTTING-0006 — CLAUDE critique of CODEX & GEMINI drafts

Comparing my draft (`PLANTPOTTING-0006-CLAUDE.md`) against the CODEX and GEMINI drafts.
All three converge on the same shape (crassula probe + two UX fixes, reuse-only), so the
differences are in precision, test-breakage awareness, and sequencing discipline.

---

## Draft A — CODEX (`PLANTPOTTING-0006-CODEX.md`)

### Stronger than mine
- **Names the pinned contract tests that my draft only gestures at.** CODEX Phase 4 calls out
  `LowConfidencePickerSubtitleContractTest` explicitly ("Update … to pin the new subtitle copy")
  and Phase 3 names `ModelScoreMapperPerSpeciesThresholdTest` and `PerSpeciesThresholdsContractTest`.
  My Phase 4 only says "Update/confirm any test asserting the old subtitle copy (search
  `low_conf_subtitle` usages)" — if a pinned contract test exists, my draft discovers the breakage
  reactively instead of planning the edit. This is CODEX's single biggest win.
- **Discrete, named CI gates.** CODEX Phase 5 splits `scripts/check-stub-isolation.sh` and
  `verifyNoNetworking` into their own checkboxes rather than folding them into
  `scripts/integration-flow.ps1` the way my Phase 6 does. More legible, and it surfaces a gate a
  reader can run independently.
- **"Run focused tests before full integration gates"** (Sequencing bullet 5) — an explicit
  cheap-failure-first ordering. My draft jumps straight to "full JVM suite + instrumented" in Phase 6
  with no focused-first step.
- **Treats subtitle-contract breakage as a named risk** ("subtitle copy changes break pinned contract
  tests; mitigation: update … in the same change"). I covered chip test-tag risk (R4) but never
  listed the subtitle-contract-test risk at all.

### Weaker than mine
- **Vaguer fixture spec.** CODEX says "crop dimensions matching the Monstera fixture convention"
  (Phase 1) where mine pins **480×480, JPEG quality 80**, exact target path. An executor reading CODEX
  has to go re-derive the Monstera encoding; mine hands them the number.
- **Shallower UX-B analysis.** CODEX recommends (b) hide-the-suffix — same conclusion as mine — but
  doesn't cite the code (`LowConfidencePickerViewModel.kt:67,87`, `LowConfidencePickerScreen.kt:81`)
  or explain *why* (a) `<1%` is dishonest: at the screen layer only the floored `Int` exists, so you
  cannot distinguish a true zero from a rounded-down zero without new data plumbing. CODEX asserts the
  recommendation; mine grounds it.
- **Drops the "keep the candidate selectable" invariant.** My Phase 5 has a dedicated checkbox that the
  zero-score chip must remain pickable (it's a picker). CODEX's chip tasks only address suffix
  formatting and never state the selectability guarantee — easy to silently regress.
- **Checkbox-noise.** CODEX makes *every* line a `- [ ]`, including Non-Goals and Risks. That defeats
  "every checkbox is a concrete work item" — a reviewer can't tell a deliverable from a constraint.
  My draft keeps checkboxes for work items only.
- **No close-out / ledger flip.** CODEX has no equivalent of my Phase 6 "Flip the sprint ledger … note
  V0.1 multi-species sweep complete." Its acceptance criteria never mention the ledger or the sweep
  declaration.

### Tasks missing from CODEX
- Ledger flip + explicit "V0.1 multi-species sweep complete" declaration.
- Exact fixture encoding (480×480 / q80) and the "test-only, not in APK" verification step
  (my Phase 1 task 4).
- An explicit "candidate stays clickable" assertion in the chip test.
- A task to re-run the **Monstera** assertion immediately after seeding `per_species_thresholds`
  (the map's first-ever populated state) — neither draft has this.

### Risks CODEX underweights
- **GMD / probe flakiness & environment drift** — my R6. CODEX assumes the probe just runs; no re-run /
  cold-warm-buildonly safety net is named as a risk.
- **Cross-class threshold contamination.** CODEX's threshold risk only covers "scores below the global
  → seed an override." It never weighs that adding the first `per_species_thresholds` entry could
  perturb `monstera-deliciosa`'s global path (override-then-global at `ModelScoreMapper.kt:55-66`).

### Sequencing CODEX gets right (vs GEMINI)
- CODEX's Phase 3 gates the assertion form and the seed decision *both* on the probe result and updates
  the mapper/contract tests in the same change as the manifest. That's the correct evidence-first
  order — better than GEMINI on this axis, on par with mine.

---

## Draft B — GEMINI (`PLANTPOTTING-0006-GEMINI.md`)

### Stronger than mine
- **Brevity / signal density.** GEMINI's two-phase structure ("Phase 1: Crassula Ovata Probing &
  Assertion", "Phase 2: UX Fixes & Cleanup") fits the whole sprint on one screen. Mine spreads the same
  work across six phases plus a dependency diagram (§5) and a 22-line trade-off essay (§6). For a
  deliberately *narrow* sprint, GEMINI sits closer to the right altitude — it reads as a task list,
  mine reads as a design doc.
- **Writes the `FakeFixedIdentifier` split as an actual checkbox.** GEMINI's last Phase 2 task — split
  into `FakeLowConfidenceIdentifier` / `FakeUnmappedIdentifier` ONLY IF a third knob appears — is a
  real, named, startable (gated) checkbox. My draft only mentions this cleanup in prose (§5, R5) and
  never gives it a checkbox, nor names the resulting fakes. A concrete completeness miss on my side.

### Weaker than mine
- **Recommends the costly/dishonest UX-B option.** GEMINI's Phase 2 recommends "Display `<1%` for
  non-zero scores that round down to zero" and calls it a "simple formatting threshold" in its risk
  section. It is **not** simple: the screen only has the floored `Int` (`probabilityPct`,
  `coerceIn(0,100)`, `LowConfidencePickerScreen.kt:81`), so `<1%` would also mislabel a *genuine* zero,
  and showing it honestly requires plumbing the pre-floor float through the ViewModel — exactly the
  "No new infrastructure" boundary GEMINI states in its own Scope Boundaries. Its risk register even
  names "Degenerate capture UI logic becomes complex," then recommends the option that *causes* the
  complexity. Mine rejects (a) for these reasons and recommends (b).
- **No verification / close-out phase.** GEMINI has *no* stub-isolation gate, no `verifyNoNetworking`,
  no `scripts/integration-flow.ps1` cold/warm/buildonly, no full-suite run, and no ledger flip. Both
  CODEX and I treat these as non-negotiable close-out gates; GEMINI omits them entirely.
- **No fixture precision.** "Centre-crop the photo to match the existing Monstera fixture" is
  unverifiable as written — mine pins 480×480 / q80 and the exact path.
- **Provenance under-specified.** "Append the photo's provenance to `LICENSE.txt`" doesn't enumerate
  the required fields. Mine lists the full block (Title / Depicts / Source / Original file / Author /
  Date taken / Retrieved / Modifications / License + attribution + test-only note) to copy verbatim
  from 0005.
- **No subtitle-test-update task.** GEMINI rewrites `R.string.low_conf_subtitle` but never finds/updates
  a test asserting the old copy — if one exists (CODEX names it `LowConfidencePickerSubtitleContractTest`),
  GEMINI's plan ships a red suite.
- **No "keep the 0% candidate selectable" guard.** Since this is a *picker*, dropping or disabling the
  zero-score chip would be a functional regression; GEMINI never states the invariant.

### Tasks missing from GEMINI
- Entire verification/close-out phase: stub-isolation, `verifyNoNetworking`, integration-flow modes,
  full JVM + instrumented suite, ledger flip.
- Subtitle contract-test update.
- Exact fixture encoding and the test-only/not-in-APK confirmation.
- The probe outcome recorded in the **named** results-doc path
  (`docs/sprints/results/PLANTPOTTING-0006.md`) tied to the existing
  `Crassula ovata → crassula-ovata` mapping-notes section (GEMINI folds both into one vague line).
- Re-run the Monstera assertion after seeding `per_species_thresholds`.

### Risks GEMINI underweights
- **Cross-class threshold contamination.** GEMINI's only threshold risk is "scores below 0.55 → seed,"
  mitigated by "the mechanism is already built." It never weighs that the *first-ever*
  `per_species_thresholds` entry could change `monstera-deliciosa` behaviour
  (`ModelScoreMapper.kt:55-66`). 0006 is the map's first populated state — that deserves a named risk
  plus a Monstera re-verify.
- **Fixture ambiguity.** No analogue of my R2 (a flowering or mixed-succulent jade photo resolving to a
  neighbouring AIY class) — for jade this is the most likely cause of a bad probe.
- **License/attribution slip** (my R3) and **GMD flakiness / environment drift** (my R6) — both absent;
  GEMINI carries only two risks total.

### Sequencing GEMINI gets wrong
- **False ordering of the UX fixes behind the ML work.** GEMINI puts all UX in "Phase 2," strictly
  after "Phase 1: Crassula Ovata Probing." The subtitle/chip fixes touch
  `strings.xml` / `LowConfidencePickerScreen.kt` and share *no* files with the probe path — they're
  fully parallelizable and should land *first* to de-risk while the GMD probe is set up. GEMINI reads
  as a hard Phase 1 → Phase 2 gate where none exists. (My §5 calls these out as parallel.)
- **Assertion form pre-committed in the task text.** GEMINI hardcodes
  `speciesId == "crassula-ovata" && !lowConfidence` as the assertion and only mentions the fallback in
  a trailing clause. The assertion form is *blocked on the probe score* and shouldn't be written until
  the number is in hand. CODEX and I keep both forms explicitly gated on the measured value.
- **Threshold seed not followed by a Monstera re-verify.** GEMINI puts the seed decision at the end of
  Phase 1 and leaves verification implicit/to CI. Correct order: probe → seed (if forced) →
  immediately re-run the Monstera assertion → proceed.

---

## If I were merging

**Keep from CODEX (A):**
- The **named contract tests** — `LowConfidencePickerSubtitleContractTest`,
  `ModelScoreMapperPerSpeciesThresholdTest`, `PerSpeciesThresholdsContractTest` — wired into the
  subtitle and threshold tasks so breakage is planned, not discovered. This is the strongest single
  contribution across either draft.
- The **discrete gate checkboxes** (`check-stub-isolation.sh`, `verifyNoNetworking`) broken out of
  integration-flow, and the **focused-tests-before-full-gates** sequencing bullet.

**Keep from GEMINI (B):**
- The **explicit, named conditional `FakeFixedIdentifier` split checkbox**
  (`FakeLowConfidenceIdentifier` / `FakeUnmappedIdentifier`) — the one place GEMINI is more concrete
  than I am; pull it into the merged list (gated).
- Its **brevity instinct** — trim my dependency diagram and shrink §6 to a 3-line recommendation so the
  plan reads as a task list, not a design doc.

**Keep from CLAUDE (mine):**
- **UX-B = drop the `(x%)` suffix at 0** (option b), with the code-cited honesty argument — *not*
  GEMINI's `<1%`, which demands float-plumbing that breaks the no-new-infrastructure boundary.
- The **exact fixture spec** (480×480 / q80, `.../identify-fixtures/crassula-ovata.jpg`) and the full
  provenance-block field list copied verbatim from 0005, plus the test-only / not-in-APK confirmation.
- The **close-out**: `scripts/integration-flow.ps1` (cold/warm/buildonly) + stub-isolation /
  network-free checks, **and the ledger flip + sweep-complete declaration**, mirrored into Acceptance
  Criteria (both other drafts drop the ledger).
- The **"keep the 0% candidate selectable"** guard.
- Risks **R2/R3/R6** (fixture ambiguity, license slip, GMD flakiness) and the **parallelize-UX-to-de-risk**
  sequencing.

**Add to all three (none has it):**
- A task to **re-run the Monstera assertion immediately after seeding `per_species_thresholds`**, with
  a matching **cross-class contamination** risk — the map's first populated state is the moment to
  prove `ModelScoreMapper`'s global path for the other class is intact.
