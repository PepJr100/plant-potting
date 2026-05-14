# PLANTPOTTING-0002 — Critique of CODEX and GEMINI drafts against CLAUDE draft

Comparator: my own draft at `docs/sprints/drafts/PLANTPOTTING-0002-CLAUDE.md`.
Reviewed: `docs/sprints/drafts/PLANTPOTTING-0002-CODEX.md` and `docs/sprints/drafts/PLANTPOTTING-0002-GEMINI.md`.

The three drafts agree on the headline scope (Bug 4, Bug 1, Bug 3 must-land; Bug 2 + UX 1 should-land; UX 2 backlog; no on-device ML; no new prod deps). The disagreements are about discipline, granularity, and what counts as "done." This critique is honest about where the other drafts beat mine.

---

## Draft A — CODEX

### What is stronger than mine

- **Scope-boundary scaffolding is cleaner.** CODEX's `## Scope Boundaries` block explicitly calls out *"Keep the existing `PlantIdentifier` seam intact and avoid production references to `StubPlantIdentifier` outside `identify/`."* That is exactly the kind of boundary an executor will trip over if you don't say it. My Phase 7 handoff note mentions it in passing; CODEX makes it a checkbox in the plan body.
- **Manifest-mode visibility as a first-class artifact.** CODEX's risk bullet *"Make the chosen script mode visible in the manifest, for example `manifest-mode=device-aware`."* names the *key* convention before the implementation tasks. My §3.7 buries the same idea inside an expected-manifest line item.
- **"Optional Backlog" framing for UX 2 is honest.** CODEX puts UX 2 under `## Optional Backlog` rather than my "Phase 6 — Optional / backlog (only if Phases 1–5 close before the sprint window ends)." Same intent, half the words, less likely to be misread as a stretch goal.
- **Bug 3 task `[ ] Ensure the script fails loudly when a device-aware run cannot find adb, cannot find a connected device, cannot install the APK, or cannot produce the required UI hierarchy evidence.`** is broader than my §3.4 — it covers not just adb but install-APK and ui-hierarchy failures too. That's a real gap in my plan.

### What is weaker than mine

- **No Phase 0 / baseline capture.** CODEX never tells the executor to run the existing CI chain green on `main` before starting. If `pixel6Api34DebugAndroidTest` is already broken on `main`, the executor will discover that at the end of the sprint, not the start. My §0.2 captures the baseline.
- **No explicit TDD-RED gating.** CODEX writes "Add `CameraScreenSmokeTest` before replacing the shutter UI" as a sequencing bullet, but does not mark `(test, RED first)` on the test tasks, and does not include the hard gate *"§2.1 test must be RED before §2.3 impl."* My §3.9 / §4 enforce this; CODEX leaves it to executor discretion.
- **No de-scope order.** When the implementer slips, CODEX has no written priority. My §4 names exactly what gets dropped first (UX 2, then Bug 2 guard, then UX 1, then build-only manifest, and never Bugs 1/2/4).
- **No dependency graph or phase sequencing diagram.** CODEX's `## Sequencing` is nine prose bullets. Mine is an ASCII tree plus hard-gate + soft-parallel sections. CODEX's prose is fine to read but harder to parallelize: a reader can't tell at a glance that Phase 3 is fully orthogonal to Phases 1, 2, 4, 5.
- **No explicit results-doc or ledger-update task in the body.** CODEX says results-file is *"if a results file is created."* That's the wrong default. The ledger move from `PLANTPOTTING-0001: in-progress` → `done` *is* the sprint's purpose; making the results doc optional is incongruent with that. My §7.2 / §7.3 nail this down.
- **No `(test)` ID convention.** CODEX intermixes tests and impl in the same bullet list. My `1.1 (test, RED first)` / `1.3 (impl)` convention makes the TDD ordering visible.

### Missing tasks (specific)

- **No `RecipeRowTag` semantic node task.** My §3.10 makes Recipe rows have a stable semantic tag so the manifest can count them deterministically. CODEX says *"capture or derive the device-aware manifest lines from the driven flow"* but never solves the *how* — leaving the executor to scrape raw text from `ui-hierarchy.xml`, which is the brittleness CODEX's own risk section warns about.
- **No `-BuildOnly` explicit switch task.** CODEX says *"explicit opt-in build-only mode if maintainers still need that path"* but never writes a task naming the switch. My §3.5 adds `[CmdletBinding()] param([switch]$BuildOnly)`. Without that task the executor will improvise.
- **No KDoc cleanup task for `PermissionScreenHost` and `CameraPermissionGuard.isGranted`.** Stale doc is part of the original review feedback; my §1.4 lands it.
- **No idempotency-guard task on `onGranted()`.** CODEX has a checkbox *"Keep `onGranted()` emission idempotent"* but the bullet is a wish; there's no test that verifies idempotency. My §1.1 asserts `onGranted` fires exactly once after resume.

### Risks underweighted

- **Sandbox filesystem overlay (per the project auto-memory) is missing.** This machine's shell tool writes can silently fail outside the project tree. My §5.6 names it. CODEX doesn't, and the script-test path it proposes (`scripts/test-integration-flow-adb-fallback.ps1`) is exactly the kind of helper that could be affected.
- **`Hilt + lifecycle + Compose instrumentation` brittleness is unaddressed.** CODEX has a risk *"Settings round-trip tests can be brittle"* but no fallback (my §5.2 names the JVM-Compose-only escape hatch via `TestLifecycleOwner`).
- **`Icons.Default.CameraAlt` may require `material-icons-extended`.** CODEX silently assumes the icon is in the core set. My §5.3 plans the contingency.

### Sequencing wrong / suboptimal

- **CODEX puts Bug 3 fix *after* the app-level tests are green** (*"Fix the integration script after the app-level tests are green so the script can be used as the final evidence gate."*). This serializes work that can run in parallel. The script change touches only `scripts/` and `docs/sprints/expected-artifacts/` — different files from Bugs 1, 4 — so a real executor can do them concurrently. My §4 "soft parallels" explicitly allows this.
- **UX 1 (bind-window) is sequenced before the Bug 2 investigation in CODEX** (*"Harden the camera bind-window state after the shutter affordance test is in place." → "Investigate Bug 2 before final acceptance"*). That ordering is fine, but CODEX never says UX 1's test scaffold *feeds* Bug 2's guard test (both need `CameraScreenTestRegistry` plumbing). My Phase 4 → Phase 5 chain makes the reuse explicit, which saves a half-day of duplicated test infra.

---

## Draft B — GEMINI

### What is stronger than mine

- **Concrete test-driving techniques I don't name.** GEMINI's §1.1 names two things mine misses: *"Simulates transition to Settings (using `Intents.intending`)."* and *"Grants permission via `pm grant` or a test hook."* Both are useful — `Intents.intending` is the Espresso-Intents API I'd want for the Settings round-trip, and `pm grant` is the deterministic shell-side grant that avoids the system permission dialog entirely.
- **Executive summary up front.** GEMINI's `## 1. Goals` block (4 numbered bullets) is the kind of TL;DR an executor reads before deciding which phase to start with. Mine has `## 1. Intent` but it's a paragraph; GEMINI's is faster to scan.
- **"Race conditions in Lifecycle" is named explicitly in risks.** My §5.1 covers the same ground but takes more words. GEMINI's one-liner *"Ensure ON_RESUME doesn't trigger multiple `onGranted()` calls if already in Granted state."* is sharper.
- **Brevity has a real cost-of-time benefit.** GEMINI's plan is ~100 lines vs my ~295. An executor will read it; mine may get skimmed.

### What is weaker than mine

- **Tasks are far too thin to drive an executor.** §2.1 says *"Land the deferred `CameraScreenSmokeTest` (§6.7 in PLANTPOTTING-0001.md)"* with two sub-bullets. My §2.1 specifies five assertions including the literal-"C" regression check and the coupling to the shutter's `ImageCapture` state. GEMINI's executor would have to re-derive those from feedback.md.
- **Manifest decision is internally inconsistent.** GEMINI §3.3 says *"Decision: Single file, script fails if device lines are missing when a device is connected."* But "fails if device lines missing when a device is connected" is not the same as "hard-fails when no device is attached." A run with no device attached would *not* produce device lines and *would* satisfy the single-file expected manifest — which is the exact bug Bug 3 is supposed to close. CODEX and I both have explicit hard-fail-on-no-device semantics; GEMINI's wording leaves the loophole open.
- **No Phase 0, no Phase 7, no ledger update task in the body.** §4 "Acceptance Criteria" mentions the ledger move but doesn't task it; nothing tasks updating `docs/sprints/results/PLANTPOTTING-0002.md` or `docs/sprints/PLANTPOTTING-0001.md` §6.7.
- **No de-scope order.** Same problem as CODEX, worse because GEMINI is shorter and the executor has less to fall back on.
- **No dependency or sequencing structure.** GEMINI uses phase labels ("Phase 1: Must-Land", "Phase 2: Should-Land") as priority tiers, not as ordered phases. There's no parallelization signal, no hard-gate, no soft-parallel.
- **Risks section is two paragraphs.** My §5 has seven risks, each with mitigation. GEMINI's two bullets cover roughly two of mine.
- **No mention of `RecipeRowTag`, no test-tag semantic-node strategy.** Same gap as CODEX, but GEMINI's §3.1 is even thinner: *"if a device is attached, ensure `ui-hierarchy.xml` is parsed to add `archetype-name` and `recipe-row-count` to the manifest."* The *how* is hand-waved.

### Missing tasks (specific)

- **No `(test, RED first)` discipline.** Tasks `1.1 (test)`, `2.1 (test)`, `4.2 (test)` exist, but there's no gate text saying the test must be red before the impl task is started. CODEX has the same gap; mine names it as a hard gate.
- **No KDoc cleanup task** (same gap as CODEX).
- **No `-BuildOnly` switch task** (same gap as CODEX).
- **No `idempotent onGranted` test task.** Idempotency is in the Risks section but not as a paired test under Bug 4.
- **No `verifyNoNetworking` or `ktlintCheck` in acceptance criteria.** GEMINI §4 says *"All new and existing tests pass (`./gradlew test connectedDebugAndroidTest`)."* — but `test` doesn't include lint/ktlint/verifyNoNetworking, and `connectedDebugAndroidTest` is the wrong target (this project uses `pixel6Api34DebugAndroidTest` for the GMD).
- **No baseline-capture / Phase 0 setup task.**
- **No sandbox-filesystem-overlay risk task** (per the project auto-memory).
- **No `Icons.Default.CameraAlt`-not-in-core-icons contingency.**
- **No `material-icons-extended` decision.**
- **No explicit `mode=device-aware` line in the expected manifest** — UX 1 §5.1/§5.2 doesn't reference test tags either.

### Risks underweighted

- **CameraX + Compose test plumbing.** Not in the risks section at all. CODEX has it; mine has it as §5.2.
- **GMD environmental flake.** Not addressed. My §5.7 makes Phase 0.2 the protection.
- **Settings round-trip + `Intents.intending` interaction with the platform permission dialog on API 34.** GEMINI suggests `Intents.intending` for stubbing the Settings intent — good — but doesn't address that `Intents.intending` can race with `pm grant` if both are used in the same test. This is the kind of trap that ate the original `PermissionDeniedFlowTest`.

### Sequencing wrong / suboptimal

- **GEMINI puts Bug 2 (§4.1, §4.2) before UX 1 (§5.1, §5.2) under Phase 2.** That's the wrong order: UX 1 establishes the `imageCapture != null` test seam, which Bug 2's `CameraPreviewStabilityTest` should rely on (same `CameraScreenTags`, same registry). My Phase 4 → Phase 5 chain shares infra; GEMINI duplicates it.
- **No explicit gate that ledger update waits for GMD-green acceptance.** GEMINI §4 lists ledger move as a criterion, but with no hard rule that ledger cannot move until §4 is observably true. CODEX and I both have this rule.
- **§3.3's "Single file" decision is in-line with the task rather than recorded in `docs/sprints/results/PLANTPOTTING-0002.md`.** Future-me will not know which decision was made or why.

---

## If I were merging, I'd keep…

- **…from Draft A (CODEX):**
  - The `## Scope Boundaries` block with the explicit `PlantIdentifier` seam / `StubPlantIdentifier` boundary (CODEX §Scope Boundaries bullet 5). This belongs at the top of my draft, not buried in the handoff note.
  - The Bug 3 task *"Ensure the script fails loudly when a device-aware run cannot find adb, cannot find a connected device, cannot install the APK, or cannot produce the required UI hierarchy evidence."* — generalize my §3.4 to cover install-APK + ui-hierarchy failures, not just adb.
  - The phrasing **`manifest-mode=device-aware`** as a named convention from CODEX's risk section, promoted into the expected-manifest spec.
  - The `## Optional Backlog` heading style for UX 2 (cleaner than my "Phase 6 — Optional").

- **…from Draft B (GEMINI):**
  - The `## 1. Goals` 4-bullet executive summary up front. Drop into my draft above `## 1. Intent`.
  - The explicit mention of **`Intents.intending`** for stubbing the Settings intent in Bug 4's test task (folds into my §1.1 alongside `TestLifecycleOwner`).
  - The explicit mention of **`pm grant`** as a deterministic permission-mutation mechanism for the instrumentation test (folds into my §1.1 as an alternative to flipping a fake guard, useful when the test must drive `MainActivity` rather than `PermissionScreenHost` directly).
  - The sharper one-liner risk *"Race conditions in Lifecycle: ensure `ON_RESUME` doesn't trigger multiple `onGranted()` calls if already in Granted state."* — replace my §5.1 phrasing with this.

- **…keep from my draft (Draft C):**
  - Phase 0 baseline capture, Phase 7 docs + ledger, the dependency graph, the hard-gate / soft-parallel split, the de-scope cascade, the `(test, RED first)` ID convention, the `RecipeRowTag` task, the `-BuildOnly` switch task, the sandbox-filesystem-overlay risk, the `Icons.Default.CameraAlt` contingency, the idempotent-`onGranted` test assertion, and the manifest-policy decision recorded in the results doc.

Net: my draft is the right backbone for the merged plan; CODEX contributes scope-boundary discipline and broader failure-mode coverage in Bug 3; GEMINI contributes the executive summary and two concrete test techniques (`Intents.intending`, `pm grant`) that my Bug 4 plan should adopt verbatim.
