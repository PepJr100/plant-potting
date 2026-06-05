# PLANTPOTTING-0010 — CLAUDE's critique of the CODEX and GEMINI drafts

Critique written from the perspective of my own draft (`PLANTPOTTING-0010-CLAUDE.md`).
The three drafts agree on the shape of the sprint (three pillars: UI/UX refresh, "Add this
plant" wireframe, text-only KB expansion, all behind the frozen identify seam + network-free
gate) and on the headline architectural move (one shared local-persistence layer for both My
Plants and the request log). The disagreements are in storage choice, sequencing, the
side-channel mechanics for confidence + strong-unmapped, and rigor on licensing / testability.

---

## Draft A — CODEX (`PLANTPOTTING-0010-CODEX.md`)

### Stronger than mine
- **Phase 0 "Baseline and Guards" is genuinely better than my Phase 0.** CODEX runs
  `testDebugUnitTest` / `verifyNoNetworking` / stub-isolation *before any edits* to catch
  environment drift, and — the part I missed — does a baseline `assembleDebug` and **records the
  current APK size** so reference-image growth has a real comparison point. My A5 size-guard task
  ("record before/after sizes in evidence") assumes a baseline I never schedule a task to
  capture. I should steal this.
- **Explicit user-initiated "save-to-collection" action** (Phase 3: "Add save-to-collection
  action from `ResultScreen.kt` and/or `RecommendationScreen.kt`"). My Phase 3 auto-appends an
  `IdentifiedPlant` on every high-confidence identification and I even flag the double-write risk
  myself ("wire from `CameraViewModel` success path *or* `ResultViewModel` init — pick one"). An
  explicit Save button is the cleaner design: it avoids polluting My Plants with every casual
  scan and sidesteps the double-write ambiguity entirely. This is a real weakness in mine.
- **Honesty acceptance criterion**: "The sprint result notes clearly separate calibrated model
  behavior from editorial KB/class-map coverage" and Phase 7's "Do not claim new mappings are
  calibrated; document them as editorial/model-vocabulary coverage." This guards the exact trap
  0009 walked up to (text-only mappings ≠ on-device calibration). I have nothing equivalent.
- **More complete file-level touchpoints** — CODEX explicitly lists `RecommendationViewModel.kt`,
  `RecommendationUiState.kt`, and `MainActivity.kt`, which I fold vaguely into "result/…". The
  reference-image render path genuinely needs `RecommendationViewModel`/`UiState` changes that my
  §8 hand-waves.
- **Concrete total APK budget** ("no more than 3-5 MiB growth over the baseline debug APK"). Mine
  gives a per-image budget (~40 KB) and "a total budget" without a number; CODEX's named ceiling
  is more actionable for a reviewer.

### Weaker than mine
- **Confidence-source subtlety is hand-waved.** CODEX populates confidence "from
  `(identifier as? CandidateProvider)?.mostRecentCandidates?.firstOrNull()` *when it matches the
  success speciesId*." My §3.2 spells out the actual failure mode: in the margin-confidence
  branch of `ModelScoreMapper`, the winning class is still `ranked[0]`, so the first *mapped*
  candidate's probability tracks `bestProb` — and I require a test pinning that, with a fallback
  to "the winning species' own score" if a future mapping diverges. CODEX's "when it matches"
  glosses over the case where it *doesn't*.
- **No bounded growth on the collection.** I cap `identifiedPlants` to most-recent-N with an
  eviction test; CODEX's `PlantCollectionRepository` has "save/list/delete or save/list minimum"
  with no cap, so the store can grow unbounded.
- **No injectable clock/time provider.** I require a `TimeProvider`/`Clock` so the store is
  unit-testable with a fake clock and a fake-clock test; CODEX writes timestamps with no
  testability seam, and its Phase 1 test list ("serialization, default empty state, increment
  idempotence, ordering") omits time.
- **No release-build exclusion guard for the debug theme switcher.** CODEX says "Keep the theme
  selector available in the debug APK" but never asserts it's *excluded from release* (my
  `BuildConfig.DEBUG` guard + "release build excludes the debug switcher" acceptance criterion).

### Tasks missing vs mine
- No `BuildConfig.DEBUG` guard / release-exclusion test for the theme switcher.
- No N-cap eviction task or test for the saved-plant collection.
- No injectable time provider + fake-clock test.
- No explicit "re-use the new DataStore to persist the theme selection" — CODEX hedges
  ("Persist … through the shared local store if cheap; otherwise `rememberSaveable` … and
  document that final theme persistence is not guaranteed"), losing the persistence synergy my
  Phase 5 banks on.

### Risks underweighted
- The **margin-confidence / `ranked[0]`** divergence (covered above) is absent from CODEX's risk
  table.
- **DataStore-vs-Room** is left genuinely open ("Prefer DataStore … unless Room becomes clearly
  necessary"). That's defensible, but a plan that doesn't *decide* invites mid-sprint
  re-litigation; my §3.1 rejects Room outright for two append-mostly collections and gives the
  reasons.

### Sequencing wrong
- **KB expansion is dead last (Phase 7), right before delivery.** This is the lowest-risk,
  content-only, merge-conflict-free, highest-guaranteed-win pillar — and CODEX schedules it where
  a sprint overrun is most likely to cut it. My Phase 1 lands it early/in parallel precisely so
  the cheapest win is banked first. CODEX's own risk table worries about "three pillars produce
  too much instrumentation churn," which makes burying the one pillar that has *no* instrumentation
  cost at the end the wrong call.
- Reference images (Phase 6) sit after themes (Phase 5) but the image-sourcing task has the
  highest external uncertainty (license availability, per 0008's 1–6.8% finding). CODEX does at
  least isolate it in its own phase — better than GEMINI — but it's still late.

---

## Draft B — GEMINI (`PLANTPOTTING-0010-GEMINI.md`)

### Stronger than mine
- **Brevity / readability of the architecture summary.** GEMINI's "Scope Boundaries &
  Architecture Decisions" compresses the four key decisions (persistence, Add-this-plant
  interception, image strategy, theme delivery) into four tight bullets that are easier to scan
  than my §3. For a principal triaging the plan, that's a real virtue.
- **Names the secondary affordance concretely**: the "Add this plant" screen has an explicit
  **"Pick manually" secondary button** routing into the existing `LowConfidencePickerScreen`. I
  describe "a secondary action to fall through to the existing low-confidence picker" — same idea,
  but GEMINI's labeling is crisper. (Near-parity, slight edge to GEMINI on naming.)
- **Concrete switcher affordance idea** ("long-press on camera" / Dev settings sheet) — a more
  tangible entry point than my generic "debug-only in-app theme switcher."

### Weaker than mine
- **Chooses Room.** This is GEMINI's biggest miss. For two small, append-mostly collections, Room
  brings KSP, schema definition, and migration ceremony that two `@Serializable` lists in a single
  DataStore document do not need — and it pulls *more* transitive dependencies onto the runtime
  classpath, raising the surface area for a `verifyNoNetworking` false-trip. Both my draft and
  CODEX prefer DataStore for exactly these reasons. GEMINI's own mitigation ("Room requires no
  network") understates the transitive-dependency risk.
- **The "Add this plant" detection mechanism is never specified.** GEMINI says "intercept strong
  OOV matches *before* the picker" but `ModelScoreMapper.candidateForIndex` returns `null` for
  unmapped labels — so a confident-but-unmapped class is *currently invisible* to the routing
  layer. My §3.3 and CODEX both extend `MappedScore` with a raw-top label/probability/`topIsMapped`
  flag and expose it via a side-channel. GEMINI's plan has no task that makes strong-unmapped
  *detectable*; the interception it promises has nothing to hang off. This is a load-bearing gap,
  not a detail.
- **Confidence display is under-specified.** "Update `ResultScreen` and `RecommendationScreen` to
  display a numeric percentage and progress bar for model confidence" — but no statement of where
  the probability comes from, how it crosses to the result screen without touching the frozen
  seam, or the margin-confidence subtlety. Both my §3.2 and CODEX route it through
  `CandidateProvider` + a nav arg; GEMINI just asserts the UI shows it.
- **Weakest licensing rigor.** Attribution goes "to an `About`/`Licenses` screen if needed, *or in
  code comments as per repository norms*." Code-comment attribution is not a defensible
  provenance record. My draft (and CODEX) require a checked-in `docs/licenses/reference-images.md`
  manifest *and a cross-check test* asserting every bundled image has a manifest entry. GEMINI has
  no manifest-validator test at all.

### Tasks missing vs mine
- No task to make strong-unmapped detectable (extend `MappedScore` / side-channel) — see above.
- No attribution-manifest validator test.
- No baseline-APK measurement (CODEX has this; I should add it; GEMINI lacks it too).
- No bounded-growth cap on the collection; no injectable clock / fake-clock test.
- No release-build exclusion guard for the debug theme switcher.
- No concrete version numbers (just "Bump the app version"); mine pins 3→4 / 0.3.0→0.4.0.
- No mention of the known transient ~1h GMD timeout or the Dropbox sandbox-write verification
  caveat — both are recorded project hazards my Phase 6 / risk table carry forward.

### Risks underweighted
- **Network-gate timing.** GEMINI adds Room (Phase 1) but only runs `verifyNoNetworking` in
  Phase 4 ("`./gradlew check`"). If the Room dependency stack trips the forbidden-substring audit,
  it surfaces at the *end*, after all feature work has stacked on top. Both my draft and CODEX
  re-run `verifyNoNetworking` *immediately* after the dependency is added. GEMINI's risk table
  lists "Network Leakage" but its sequencing doesn't act on it.
- **Heterogeneous-sprint sprawl** is not in GEMINI's risk table at all — and GEMINI's four-phase
  structure with all three "new features" crammed into Phase 3 makes overrun *more* likely, not
  less.

### Sequencing wrong
- **Reference images are bundled in Phase 3 alongside My Plants and Add-this-plant** — the
  highest-external-uncertainty task (license availability) co-mingled with the two highest-effort
  feature builds. If image sourcing stalls, it stalls inside the feature phase. CODEX isolates
  images in their own phase and I put them in the existing-screens polish phase (Phase 2),
  independent of the persistence-dependent features. GEMINI's bundling is the riskiest of the three.
- **Themes (Phase 2) land before the persistence layer is proven**, yet GEMINI uses a "temporary
  debug menu" rather than persisting the selection — so it never reaps the DataStore-reuse synergy
  that both my Phase 5 and (optionally) CODEX's Phase 5 capture. Doing themes early *and* throwing
  away the persistence reuse is the worst of both orderings.

---

## If I were merging

I'd keep my own draft's spine — **DataStore over Room** (§3.1), the **margin-confidence
`ranked[0]` handling + test** (§3.2), the **`MappedScore` raw-top side-channel** for
strong-unmapped detection (§3.3), the **CC0/PD-only manifest + cross-check test** (§3.4), the
**N-cap eviction + injectable clock** tests, and **KB expansion early/parallel** — and graft:

- **From draft A (CODEX):**
  1. Its **Phase 0 baseline capture** — run the full gate suite *before* edits, and do a baseline
     `assembleDebug` to record the starting APK size so the image-size delta has a real anchor
     (fixes a genuine hole in my Phase 0/A5).
  2. The **explicit user-initiated "Save to My Plants" action** from `ResultScreen` instead of my
     auto-append, eliminating the double-write ambiguity I had to flag against myself.
  3. The acceptance criterion **"separate calibrated model behavior from editorial KB/class-map
     coverage"** in the sprint notes.
  4. The named **3–5 MiB total APK budget** alongside my per-image ~40 KB cap.
  5. The explicit `RecommendationViewModel` / `RecommendationUiState` / `MainActivity` touchpoints.

- **From draft B (GEMINI):**
  1. The explicitly-labeled **"Pick manually" secondary button** on the Add-this-plant screen.
  2. The **concrete switcher affordance** (long-press on camera / Dev settings sheet) as the
     entry point for my debug-only, DataStore-persisted theme switcher.
  3. Its **tight four-bullet architecture-decisions summary style** as a scannable preface to my
     fuller §3 — readability without losing the rigor.

Net: GEMINI contributes presentation polish but little load-bearing engineering (its Room choice,
missing strong-unmapped mechanism, and code-comment attribution are all things I'd drop); CODEX
contributes the most substantive grafts (baseline APK capture, explicit save, calibration-honesty
criterion). The one place I'd override CODEX outright is **sequencing**: KB expansion belongs
early, not in Phase 7.
