# PLANTPOTTING-0005 — Critique of CODEX and GEMINI drafts (from CLAUDE)

Honest read of where the other two drafts beat mine, where they fall short, where they miss tasks, where they underweight risk, and where they sequence work wrong. My draft is `PLANTPOTTING-0005-CLAUDE.md`; the others are `PLANTPOTTING-0005-CODEX.md` and `PLANTPOTTING-0005-GEMINI.md`.

---

## 1. CODEX draft

### Stronger than mine

- **"Working Decisions" section (Codex §Working Decisions).** Codex carves out durable project-policy bullets — "Preserve honest confidence language", "Keep `ResultScreen` source badge behavior unchanged", "Keep the real-model test path production-shaped". Mine scatters this kind of policy across §4 (Decisions) and §4.8 (Seam invariants) — Codex's framing is cleaner because it separates *project values* from *option/rejected-option dialectics*. Worth lifting.
- **Production-shape regression guard for the real-model test (Codex §Phase 1, bullet "Add or update an androidTest assertion that the real-model smoke test uses `OnDevicePlantIdentifier` and does not resolve `PlantIdentifier` to `FakeFixedIdentifier`").** This is a concrete defence against the *exact* footgun PLANTPOTTING-0004 §4.5 deferred: after the global swap is gone, what stops a future test author from re-introducing a different global fake and silently downgrading `OnDeviceModelRealInterpreterTest`? Codex asserts the binding identity. Mine assumes "no global swap" is sufficient — it isn't if someone adds a new one.
- **Captured Failure-state flush (Codex §Phase 3, "Add a ViewModel test that verifies a new capture attempt clears or supersedes the prior `CameraUiState.Failure`").** I didn't task this. The new banner is persistent (unlike the old top-center `Text` which the user might dismiss-by-scrolling); if a second capture fires without clearing the prior `Failure` reason, the banner could lie. Codex catches the bug surface that the polish-itself creates.
- **Concrete capture-drive mechanism (Codex §Phase 4, "Drive the flow by invoking `ViewModelProbe.findCameraViewModel()?.onCaptureReady(...)` rather than depending on a real camera frame").** Mine waves at the test using `@BindValue FakeFixedIdentifier(lowConfidence = true)` but doesn't name *how* the test gets a JPEG into the pipeline on GMD. Codex names the seam (`ViewModelProbe`) — a real implementation detail I should not have to re-discover at execute time.
- **Small-screen viewport assertion (Codex §Phase 2, "Add a small-screen Compose assertion that the search field and at least one selectable species row are reachable without layout overlap").** Mine sets the polish target qualitatively ("looks finished") but doesn't lock the layout against regressing into a state where search drops below the fold. Codex turns the polish into a falsifiable measurement.
- **Anti-overfit guard (Codex §Phase 6, "Do not tune thresholds to force a false green result; if the real photo does not support a direct Monstera assertion, keep the candidate-bearing assertion and document the measured limitation").** Stated as an explicit non-action. My §4.7 has the same spirit (rejecting the optimistic-then-fix-forward pattern) but Codex's flat prohibition is harder for an executor to drift past.
- **Test inventory specificity (Codex §Phase 0, "List every androidTest class launched through `MainActivity`")** enumerates `CameraScreenBoundStateTest` — a class I missed entirely in my §3.1 migration list (I had six tests; Codex implies seven). My §0.4 *tells* the executor to "grep `app/src/androidTest/` for tests that compile against the global swap and reconcile against the six" — Codex hands them the explicit list.
- **Manual emulator walkthrough as a deliverable (Codex §Phase 8, "Manually walk the app on the emulator through permission granted → camera → shutter → ...").** Mine treats GMD + Robolectric as sufficient evidence; Codex bakes in a "use the app" pass before close. UI polish without a hands-on walk is exactly how landing-feels-finished claims regress.
- **Retrieval date in fixture metadata (Codex §Phase 6, "source, author when available, license, URL, retrieval date").** I had attribution + licence + URL; Codex adds the date. Cheap; useful when a Wikimedia file gets edited or re-uploaded under a different licence later.
- **Calibration-notes documentation tie-in (Codex's threshold link in `docs/kb/ml-mapping-notes.md` — actually that's Gemini; Codex doesn't have this, scratch).** *(See Gemini section below.)*

### Weaker than mine

- **No baseline RED contract-lock pattern.** Codex does not propose RED-first contract tests for `TestIdentifyModule` absence (mine §0.6), `perSpeciesThresholds` field existence (mine §0.7), or `LowConfidencePicker SUBTITLE` tag (mine §0.8). The whole sprint's falsifiability story is weaker — Codex's gates are "this passes", mine's are "this *now* passes when it did *not* before". For a multi-track sprint with a refactor + UI polish + calibration scaffold concurrently, the RED-first lock is how you know each track actually moved.
- **No probe-then-assert workflow for the accuracy assertion.** Codex says "choose the stricter assertion only after measuring the actual model output" (Phase 6), which is the right *spirit*, but doesn't carve out a separate **probe step** that captures numbers into the results doc *before* the assertion lands (mine §5.5 + §5.6). Without that, the executor decides the assertion form privately at edit time, and the decision history is lost. The probe-into-results pattern makes the choice auditable.
- **No fallback assertion form pre-committed.** Codex hedges ("either a high-confidence equality or a low-confidence candidate-contains"), but I pre-commit the preferred + fallback assertion strings in §5.6. An executor under deadline pressure will pick the easiest passing form; explicit fallback wording stops them from inventing a third weaker form.
- **No `-BuildOnly` integration-flow run.** Codex Phase 8 runs `integration-flow.ps1 -BuildOnly` and the booted run, which is good — but does not insist on the cold *and* warm forms separately. Mine §7.3 demands cold + warm + `-BuildOnly` (matching the 0001/0003/0004 pattern). The cold/warm split catches state-caching regressions; folding them is a coverage loss.
- **No explicit `@Ignore` count == 0 audit.** Codex un-ignores `PermissionDeniedFlowTest` (Phase 5) and lists it in acceptance, but doesn't have a `grep -R "@Ignore" app/src/androidTest/ → zero hits` post-condition like mine §7.5. Without the audit, a future "temporarily ignored" sneak-in is invisible.
- **Banner-vs-Snackbar is left undecided.** Codex §Phase 3 says "banner or bottom sheet-style surface, reusing the existing camera route". An executor picks at edit time. Mine §4.2 commits to Material3 `Banner`-style `Card` anchored bottom-center with rejected-options reasoning. Indecision in a sprint plan becomes a design decision at PR review, which is where decisions go to slip.
- **Less concrete copy.** Codex says "rewrite headline and supporting copy so the first line explains uncertainty"; mine specifies `R.string.low_conf_subtitle = "This model recognises a limited plant vocabulary — please confirm or pick below."`. Pre-committing copy means the executor isn't both engineer and copywriter.
- **No icon-source decision.** I called out the `material-icons-extended` dependency trap (mine §1.7) — adding the dep for one icon is bloat; inline vector is the right call. Codex doesn't address it; the executor may pull the full dep.
- **Ledger update is implicit.** Codex never says "flip the ledger to in-progress at start, done at close". Mine has it as §0.3 and §7.7. Easy to miss.

### Tasks missing vs mine

- Contract-lock RED-first tests (mine §0.6, §0.7, §0.8).
- Baseline capture to a file (mine §0.2 captures to `PLANTPOTTING-0005-baseline.txt`; Codex Phase 0 only says "capture the output path planned for...").
- `PermissionScreenTags` open-settings tag confirmation step (mine §0.5).
- Explicit `R.string` additions (`low_conf_subtitle`, `low_conf_no_candidates`, `low_conf_search_empty`, `camera_failure_retry`, `camera_failure_banner_content_description`).
- Outlined-button visual-de-emphasis decision for the archetype CTA (mine §1.6; Codex preserves `PICK_BY_ARCHETYPE` tag but doesn't reframe the visual weight).
- Probe-code removal audit (mine §7.6 + risk §7.6).
- Trailing chevron icon on candidate chips (mine §1.7).
- Per-phase intermediate verify capturing output to `-phase{N}-verify.txt` (mine §1.8, §2.6, §3.9, §4.6, §5.7) — Codex does focused class runs but doesn't ladder them into evidence files.
- File-size budget on the fixture (mine §5.4: <=200 KB JPEG-q80 480×480).

### Underweighted risks

- **Probe-code shipping accidentally.** Codex doesn't have an equivalent risk to mine §7.6. The probe is explicitly temporary code; some final-verify must grep for it.
- **`per_species_thresholds` map accidentally seeded.** Codex says (Working Decisions) "keep calibration modest" but doesn't guard against an executor seeing the empty map and helpfully filling it from probe numbers. Mine §7.7 calls this out.
- **Material3 banner tap-target collision with shutter (FAB).** I covered this (mine §7.5: `padding(bottom = 144.dp)` and verify shutter stays enabled). Codex's "no overlap with the shutter" check (Phase 3) is good but doesn't size the padding or call out the FAB elevation interaction.
- **Intents-test leakage on GMD (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`).** I addressed this (mine §7.10) — `Intents.init()`/`release()` lifecycle, possible `IntentsTestRule` fallback. Codex Phase 5 only says "Use Espresso Intents to assert tapping the open-settings action emits..." — assumes the intent is intercepted but doesn't verify it.
- **`@BindValue` + `@JvmField` Kotlin trap.** I noted this (mine §4.3 migration ergonomics, §7.1 risk + per-test compile-check after each migration). Codex notes "compile androidTest after each migration batch" — *batch* is weaker than *per-test*. The Hilt generated-graph error blames the generated file, not the source line; compile after every test is the only way to localise the breakage.

### Sequencing — where Codex is wrong

- **Codex Phase 1 = test-wiring migration before any UI changes.** This is correct in principle but it places the largest single-mistake-surface (six tests, `@BindValue` migration) at the *start* of the sprint, before the executor has built up muscle on the codebase. Mine sequences the UI polish (Phases 1–2) before the test refactor (Phase 3) because the polish is lower-risk and a small productive run warms up the executor before the high-risk migration. Codex's order is defensible — the migration unblocks `LowConfidenceFlowTest`'s `@BindValue` — but neither sequence is wrong; mine has a softer ramp.
- **Codex Phase 4 (LowConfidenceFlowTest) comes after Phase 1 (migration) and after Phase 2 (LowConfidencePicker polish).** I have it as Phase 4 (after migration in Phase 3 and after polish in Phase 1) — same order. Fine.
- **Codex Phase 6 (Calibration) only after the Hilt migration.** Reasonable — the real-model test path is "production-shaped" only after `TestIdentifyModule` is gone. Mine has Phase 5 (calibration) similarly downstream. Fine.
- **Codex Phase 7 (ROADMAP) before Phase 8 (final-verify).** Sensible because ROADMAP names the post-sprint state, which is known once all phases land. Mine has it at Phase 6 with the same intent. Fine.
- **One bug:** Codex Phase 5 ("Run only `PermissionDeniedFlowTest` on GMD before adding it back to the full suite") implies it's been removed from the suite — it hasn't; it's `@Ignore`'d in place. The bullet should say "before re-running the full GMD suite". Wording slip.

---

## 2. GEMINI draft

### Stronger than mine

- **Concise.** ~100 lines vs my 450. A senior reviewer can hold Gemini's plan in their head; mine they have to re-grep. For an experienced AI implementer who already knows the repo, Gemini's brevity is a feature. (For a less experienced implementer, mine's specificity wins.)
- **Calibration-note tie-in (Gemini §3.3, "Add a manual 'Calibration' note to `docs/kb/ml-mapping-notes.md` explaining how thresholds were determined").** I task `docs/ROADMAP.md` but don't extend the existing ML mapping notes with calibration provenance. Gemini's link to the ML-mapping doc is the right place to record *why* a per-class threshold landed at value X — and it survives sprint result rotation. Steal this.
- **"Threshold sprawl" risk framing (Gemini §5, second risk).** Clear, short label for a real failure mode. My §7.7 covers it but Gemini's phrasing is more memorable for an executor making a quick judgement call ("am I about to cause sprawl?").
- **Limits the calibration tuning surface to "the 2/18 species in the AIY vocabulary".** Wait — this contradicts the "no per-species values seeded this sprint" non-goal in mine §2.3. Gemini is actually proposing to *seed* values for the in-vocab species. This is **bolder** than my plan and arguably better — it lands real calibration values, not just an empty mechanism. (See "weaker" below for the downside.)

### Weaker than mine

- **Way too thin on tasks.** §3.1 has 5 bullets covering Phase 0 + Phase 3 (infra + Hilt migration). My equivalent (Phases 0 + 3) has 17 tasks. "Migrate other instrumentation tests (total 6) to use `@BindValue`" is one line — that's one task standing in for what *should* be six explicit per-test migrations with a compile-check between each. An executor will absolutely batch and break.
- **Hard-coded `score > 0.6` assertion (Gemini §3.3, "Update `OnDeviceModelRealInterpreterTest.kt` with an accuracy assertion (e.g., score for *Monstera deliciosa* > 0.6)").** This is the optimistic-and-fix-forward pattern my §4.7 explicitly rejects. There is no probe step. There is no fallback. If the real photo lands the model at 0.52 (below the `high_confidence_plain` 0.55 threshold), the assertion fails, the executor either reduces the bar or burns a follow-up commit. The value 0.6 is plausible but arbitrary — and arbitrary numbers in assertions are exactly how test brittleness creeps in.
- **No baseline run.** Gemini's §3.4 lists the gate chain to run *at the end*. No equivalent of mine §0.2 ("run the gate chain on clean main *before* edits to know what was already red"). If a baseline test was already broken pre-sprint, Gemini's plan attributes the breakage to the sprint's edits.
- **No contract-lock pattern.** No RED-first tests for the deleted module, the new `perSpeciesThresholds` field, or the new subtitle tag. Same coverage gap as Codex; Gemini doesn't even gesture at it.
- **Risk section has two items.** Mine has ten; Codex has eight. Two-item risk sections in a polish-plus-un-defer sprint with three independent tracks miss far too much surface — fixture licence compatibility, probe code leaking, banner/FAB tap collision, `@BindValue`/`@JvmField` finicky behaviour, fake guard insufficient state, photo routing low-conf despite real data, intents-test leakage on GMD. Any one of these eats half a day if it bites.
- **"Snackbar or Material Banner"** (Gemini §3.2) — same indecision as Codex. Picks none of the alternatives, doesn't document why. My §4.2 commits and explains.
- **"Material Banner" capitalisation suggests a Material3 component that doesn't exist as a first-class composable.** Material3 Compose ships `Snackbar` but no `Banner` composable yet — both Codex and Gemini gloss over this; mine §2.3 calls out building it as a `Card` (since `Banner` requires building yourself or importing the M2 component). Minor accuracy thing but suggests neither caught the actual API surface.
- **No `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` discussion.** Mine §2.3 explicitly commits to no re-baselining; Gemini omits this. The polish *could* alter resource IDs the integration-flow script greps for if test tags change — failing to discuss this means the executor discovers the issue at integration-flow run time.
- **`ArchetypePickerScreen` naming (Gemini §3.2).** Gemini writes "navigates to `ArchetypePickerScreen`". The current `LowConfidencePickerScreen` already has an inline "Pick by archetype" path — it's not clear whether `ArchetypePickerScreen` exists as a separate route in the current codebase or whether Gemini is implicitly proposing a new screen. Either way, the wording is sloppy.
- **`PermissionDeniedFlowTest` re-enable spec is wrong (Gemini §3.4, "Verify `ACTION_APPLICATION_DETAILS_SETTINGS` intent fires when 'Grant' is clicked in denied state").** The *whole point* of the original `@Ignore` reason was that "Grant" in the permanent-denied state is conventionally re-labelled "Open Settings" — the button is the same tag but the action and copy differ. Gemini conflates them. My §4.6 + §4.4 disambiguates by referring to the "Open Settings" button under `PermissionScreenTags` (and tasks confirming the tag at §0.5).
- **No ledger update.** Gemini's §6 acceptance criteria doesn't mention `docs/sprints/ledger.yaml` at all. The ledger is the sprint state source-of-truth; not closing it leaves the sprint *visibly* incomplete in `ledger.yaml`.

### Tasks missing vs mine

- Baseline capture (mine §0.2).
- Contract-lock RED-first tests (mine §0.6, §0.7, §0.8).
- Probe-then-assert workflow (mine §5.5 + §5.6) — Gemini just commits the assertion.
- `@Ignore` count audit (mine §7.5).
- `testTagsAsResourceId` regression audit (mine §7.5).
- `-BuildOnly` integration-flow run (mine §7.3).
- Cold + warm integration-flow split (Gemini lumps as one run in §3.4).
- Ledger update at sprint start and close.
- Fixture file size budget.
- `R.string` resource keys for new copy.
- Per-phase intermediate verify capture files.
- Open-Settings tag confirmation task (mine §0.5).
- `FakeFixedIdentifier` constructor extension for `lowConfidence = true` (mine §3.1).
- `FakeCameraPermissionGuard` `permanentlyDenied` flag extension (mine §4.3).
- Probe-code removal audit (mine §7.6).
- README post-sprint state update + ROADMAP link (mine §6.2 — Gemini creates the ROADMAP but doesn't link it from README, so a casual reader of the repo never finds it).

### Underweighted risks

- **Real photo licence incompatibility** — Gemini's §3.3 says "CC-licensed photograph" but no licence-verification task and no risk entry.
- **Probe code shipping accidentally** — no equivalent risk.
- **Banner/FAB tap collision** — no equivalent risk; Gemini doesn't address layout.
- **`@BindValue`/`@JvmField` finicky** — no risk entry; only generic "PermanentlyDenied is hard to test" + "threshold sprawl".
- **Photo routes low-confidence despite being real** — no risk entry, despite the hard `> 0.6` assertion.
- **`FakeCameraPermissionGuard` insufficient state** — no risk; the entire mitigation is "Rely on `FakeCameraPermissionGuard`" without asking whether the fake currently supports the needed state.
- **Intents-test leakage** — not addressed.
- **Integration manifest resource-id drift from UI polish** — not addressed.

### Sequencing — where Gemini is wrong

- **Gemini Phase 1 = "Foundation: ROADMAP.md + Hilt migration".** Putting ROADMAP at Phase 1 is wrong: ROADMAP should describe the *post-sprint* state, so writing it at Phase 1 produces a doc that goes stale by Phase 4. Mine puts ROADMAP at Phase 6 (after all functional phases). Gemini's "foundation" framing is rhetorically nice but practically wrong — the ROADMAP is summary work, not setup work.
- **Gemini Phase 2 = UI Polish *before* the test refactor (Phase 1) lands.** Wait — Gemini Phase 1 includes the Hilt migration, so this is actually consistent: migration in Phase 1, polish in Phase 2. Fine.
- **Gemini Phase 3 (Calibration) before Phase 4 (LowConfidenceFlowTest).** Putting calibration before the instrumentation tests is fine because the tests use fakes; the real model doesn't enter the flow test. But there's a hidden coupling: if the new `perSpeciesThresholds` field is parsed wrong, the JVM unit tests in calibration will fail and the executor will spend Phase 3 debugging parsing — landing them after the flow test would not have helped, so this is fine.
- **Gemini Phase 4 lumps `LowConfidenceFlowTest` and `PermissionDeniedFlowTest` together.** Mine does too (Phase 4). Fine.
- **No Phase 0 / baseline.** This is the biggest sequencing miss. There's no "before edits, capture the state" gate. If the executor lands the polish and then a Phase 4 GMD run reveals a pre-existing flake, Gemini's plan attributes that flake to the sprint.

---

## 3. If I were merging

**Keep from CODEX:**

- The **"Working Decisions" section** as a top-level frame for project-policy bullets (separates values from option-evaluation dialectics). Lift verbatim into the merged plan.
- The **production-shape regression guard** ("Add or update an androidTest assertion that the real-model smoke test uses `OnDevicePlantIdentifier`"). My plan needs this — without it the `TestIdentifyModule` removal is unguarded against re-introduction.
- The **`ViewModel` test for clearing prior `CameraUiState.Failure`** on a new capture attempt. Tasks the bug surface the new persistent banner creates.
- The **explicit androidTest class enumeration** in Phase 0, including `CameraScreenBoundStateTest` — and a per-class migration check, not a batch one.
- The **`ViewModelProbe.findCameraViewModel()?.onCaptureReady(...)` capture-drive mechanism** named in `LowConfidenceFlowTest`. Concrete seam, removes a discovery hop.
- The **small-screen viewport assertion** for `LowConfidencePicker` ("search field and at least one selectable species row reachable without layout overlap").
- The **anti-overfit explicit prohibition** ("Do not tune thresholds to force a false green result").
- The **manual emulator walkthrough** as a Phase 8 evidence deliverable.
- The **retrieval-date** addition to the fixture LICENSE metadata.

**Keep from GEMINI:**

- The **`docs/kb/ml-mapping-notes.md` calibration provenance note** (§3.3). Pair it with mine §5.5 probe output — the probe goes in `results/PLANTPOTTING-0005.md`, the *threshold-rationale* writeup goes in `ml-mapping-notes.md`. Two locations, two audiences.
- The **"threshold sprawl"** risk label — adopt the phrase even if my risk inventory is otherwise more comprehensive.
- The **boldness of seeding actual per-class threshold values** for the 2/16 in-vocab species *if and only if* the probe (mine §5.5) produces evidence those overrides are warranted. I'd reframe my non-goal §2.3 ("seeding per-species threshold values") to "no seeding *unless* the probe shows a specific in-vocab species fails the global threshold by a margin a per-class override would close". That preserves the "no calibration sprint here" guardrail while allowing the one productive override Gemini gestures at.

**Keep from CLAUDE (mine) and reject from both others:**

- Contract-lock RED-first tests (mine §0.6, §0.7, §0.8). Both other drafts skip this and the sprint's falsifiability suffers.
- Probe-then-assert workflow with pre-committed preferred + fallback assertion forms (mine §5.5 + §5.6 + §4.7).
- Explicit baseline capture (mine §0.2).
- `@Ignore` count audit (mine §7.5).
- Banner-vs-Snackbar decision (mine §4.2) — both other drafts leave it open.
- Specific `R.string` keys (mine §1.1, §1.3, §1.5, §2.5).
- Material3 `Banner` accuracy footnote — note that there is no first-party `Banner` composable in Material3 Compose; build as a `Card`.
- Per-phase intermediate verify captures (mine §1.8, §2.6, §3.9, §4.6, §5.7).
- Probe-code removal audit (mine §7.6).
- ROADMAP placement at Phase 6 (post-functional, pre-final-verify), not Phase 1 as Gemini proposes.
- The full 10-risk inventory.
