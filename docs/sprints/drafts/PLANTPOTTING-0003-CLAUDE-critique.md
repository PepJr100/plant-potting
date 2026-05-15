# PLANTPOTTING-0003 — CLAUDE's critique of CODEX and GEMINI drafts

Reviewer: CLAUDE draft (`PLANTPOTTING-0003-CLAUDE.md`)
Targets: `PLANTPOTTING-0003-CODEX.md`, `PLANTPOTTING-0003-GEMINI.md`

User intent is locked: on-device ML, fully offline, model shipped under `assets/`, Bug A and UX 2 bundled. I do not relitigate any of those. I critique model choice, low-confidence UX, mapping strategy, test pairing, risk coverage, sequencing, and acceptance.

---

## 1. CODEX (`PLANTPOTTING-0003-CODEX.md`)

### 1.1 What is stronger than mine

- **§0.4 `PlantIdentifierContractTest`** — a Phase 0 RED test that pins the interface shape (one suspend `identify(ByteArray)`, plus the three required `IdentificationResult` fields) before any other work. My §4.4 "seam invariants" is prose; CODEX bakes the invariant into a test that runs on every push. This is strictly better TDD discipline for the load-bearing constraint.
- **§2.6 / §2.7 `model_manifest.json` + `ModelManifestTest`** — CODEX records source URL, license, input contract, label count, **and a SHA hash of the model bytes**, then asserts the hash matches at test time. My §4.5 just lists the files. If the model is ever silently re-downloaded or re-uploaded, CODEX catches it; I don't.
- **§3.5 mapping coverage of aliases and genus-level entries** — CODEX explicitly calls out `Sansevieria trifasciata`-as-alias-for-`Dracaena trifasciata`, `Calathea orbifolia`-as-alias-for-`Goeppertia orbifolia`, and genus-level `Phalaenopsis`. Mine names the same three but only as JSON-inline examples without explaining the editorial rationale; CODEX's task list spells out *why* each alias category exists.
- **§3.9 "all 16 KB species reachable" golden test** — proves coverage across the union of model-direct mapping and the manual picker. I have a one-way `PlantClassMapValidationTest` (mapping values exist in KB) and only mention the inverse `KbToModelCoverageTest` as a deferred follow-up in §7.4. CODEX builds the inverse check in this sprint.
- **§3.5 explicit "no mapping for labels that would create unsafe advice"** — a load-bearing safety policy for the mapping author. My §4.2 is silent on this; I implicitly trust the mapping author. CODEX writes the policy down.
- **§4.4 `InterpreterFacade` abstraction** — CODEX wraps TFLite's `Interpreter` so unit tests don't instantiate the native runtime. My §3.4 `OnDevicePlantIdentifierThresholdsTest` is supposed to do the same job by "constructing fixture `Classifications` objects directly" but the seam is less crisp; CODEX's facade is the cleaner, more testable abstraction and pays back in Phases 4 + 5 + 9.
- **§9 `docs/ml/model-card-aiy-plants-v1.md` plus `ModelCardContentTest`** — a real model card committed alongside the artifact, structurally validated. My §7.2 just promises a `docs/kb/ml-mapping-notes.md` editorial doc. The model card is the recognised industry artifact and is the right thing to ship next to the `.tflite`.
- **§1.3 honest accuracy ballpark** — CODEX gives "40–65 percent top-1 and 60–80 percent top-3 on well-framed plant photos after KB mapping, with lower performance on variegated cultivars such as `Philodendron erubescens 'Pink Princess'`" and explicitly says *the sprint measures on a fixture corpus, not claims field accuracy*. My §4.1 quotes Google's own card numbers (67% / 84%) without that hedge. CODEX is more intellectually honest about the post-mapping degradation.
- **§5.6 risk on low-confidence exception ergonomics** — CODEX names the "exception-as-control-flow" smell of `LowConfidenceIdentificationException` and accepts the tradeoff with a mitigation (named domain exception, explicit catch in `CameraViewModel`, first-class UI state). I avoid the issue by putting `lowConfidence: Boolean` on the result, but CODEX at least *thinks* about it.

### 1.2 What is weaker than mine

- **Model version pinning is loose.** CODEX says `google/aiy/vision/classifier/plants_V1/1` and waffles between "float TFLite variation if available" vs "metadata-bearing quantized variation only if the float artifact is not distributed." My §4.1 commits to `…/plants_V1/3` FP16 with a measured 25.4 MB on disk and 120–180 ms inference. An implementer reading CODEX has to make the variant choice themselves.
- **Threshold policy is split and harder to reason about.** CODEX's §1.5 has two acceptance rules (`score>=0.55 AND mapped` OR `score>=0.45 AND margin>=0.18`) plus an archetype-prefilter rule (top-3 mapped agree on archetype with total>=0.60). My §4.3 is a single-axis table keyed on `bestProb` with three contiguous bands. Mine is easier to defend and easier to test row-by-row (which is exactly what my §3.4 `OnDevicePlantIdentifierThresholdsTest` does).
- **`LowConfidenceIdentificationException` mutates the seam's effective contract.** Even though `PlantIdentifier.identify(...)`'s *signature* is unchanged, callers now have to know to catch a domain exception or the app crashes. The seam contract is "callers expect a result they can route on." My §3.3 explicitly forbids throwing from the identifier and routes everything through `IdentificationResult` shape; CODEX changes the de facto contract without changing the interface declaration.
- **Phase 1 (Bug A) is shown as serial step 1 in §4 ASCII but text in §4.2 says "Phase 1 can land in parallel."** Inconsistent. My §6 / §6's diagram explicitly marks Phase 6 as a parallel side-branch.
- **No `Start-Sleep` budget reasoning.** CODEX's §1.8 adds a 2-second settle but doesn't reconcile against `Wait-ForNode`'s existing retry budget. My §6.4 raises `maxAttempts` from 8 to 12 and explains the combined ~12 s budget. Without this reasoning, CODEX's fix may flake on cold AOSP boots.
- **No "Transcript A / B / C" structure for Bug A evidence.** CODEX's §1.10 says "record whether the device-aware diff passes" with no breakdown for cold-start vs warm-start vs `-BuildOnly` regression. My §6.5 requires three named transcripts to prove the race is dead across launch states.
- **No retroactive PLANTPOTTING-0002 §310 acceptance tick.** Bug A blocks an open §8 acceptance line in PLANTPOTTING-0002 and CODEX never closes that loop. My §6.6 + §7.3 retroactively tick it.

### 1.3 What tasks are missing

- No task to **rename or relocate the existing `IdentifyModule`**. CODEX §5.2 says "Replace the production binding" but the current module's filename and `@Binds` shape isn't audited. My §3.5 spells out the two acceptable migration shapes (rename to `StubIdentifyModule.kt` vs delete-and-re-bind-from-test source set) and asks the implementer to document the choice.
- No task to **audit `aaptOptions { noCompress 'tflite' }` against duplicate-resource conflicts**. CODEX §2.3 mentions packaging "if AGP requires it" but doesn't actually run the audit. My §1.4 / §1.5 does.
- No task on **APK-content verification of `assets/ml/...`** at integration-script level. CODEX §2.1 has a `ModelAssetsTest` (good), but doesn't extend `scripts/integration-flow.ps1`'s APK-inspection step to assert the assets ship. My §8 acceptance line ties this to the integration script directly.
- No task to **draft `ml-mapping-notes.md`-style editorial justification per mapping line**. CODEX's model card (§9.3) covers high-level mapping strategy, but not per-row reasoning. My §2.5 requires one paragraph per `plant_class_map.json` entry citing why the model label routes to that KB species id, mirroring `plant-substrate-kb-notes.md`.
- No task to **add a stretch "see other candidates" disclosure on the high-confidence path**. CODEX's manual picker is only reachable on low confidence; mine has it as a §3.2 nice-to-have for the high-confidence path so users with a wrong "confident" answer can still recover. CODEX's design has no escape hatch from a wrong-but-confident result.

### 1.4 What risks are underweighted

- **TFLite x86_64 ABI on the GMD** — CODEX's §5.3 mentions "TFLite native libraries can introduce ABI or packaging friction" but doesn't name the specific concern that `pixel6Api34` GMD is x86_64 and that `tensorflow-lite`'s native `.so` must include `x86_64`. My §7.5 names it and confirms the default ABIs cover it.
- **`@TestInstallIn` + Compose + CameraX + TFLite test-config burn** — PLANTPOTTING-0001 §7.7 and PLANTPOTTING-0002 §6.2 both flagged this combo as a half-day sink. CODEX has no equivalent to my §7.8. The first time the implementer hits a Hilt test-binding issue while debugging an instrumentation-side TFLite load, they will lose hours.
- **Sandbox filesystem overlay on Windows** — CODEX has no equivalent to my §7.11. Per `~/.claude/projects/.../memory/sandbox_filesystem_overlay.md`, sub-shell writes outside the project tree may not reach disk on this machine. This bites when the implementer writes evidence files to ad-hoc paths.
- **Mapping table staleness when KB grows** — CODEX's §5.1 covers "model labels don't cover all 16 KB species" but not the inverse: future KB-species additions silently leaving labels unmapped. My §7.4 names this and proposes `KbToModelCoverageTest` (even if deferred).

### 1.5 What sequencing is wrong

- **Phase 1 (Bug A) presented serially before Phases 2–9 in the §4 ASCII diagram, but §4.2 text says it's parallel.** Pick one. My §6 makes Phase 6 (Bug A) a side-branch that runs concurrent with 1–5 in both diagram and text.
- **§9 (fixtures + model card) lands before §10 (offline gates)**, but §10 includes the GMD test. If a fixture image breaks the GMD test, you discover it after spending model-card time. Better to run the offline gates as a fast-feedback loop earlier (my §3.7 runs `check-stub-isolation.sh` and unit tests immediately after the OnDeviceIdentifier lands, then `verifyNoNetworking` immediately after the deps in §1.2).
- **Phase 6 (camera source plumbing) before Phase 7 (badge) before Phase 8 (manual picker)** is fine, but Phase 6 hard-blocks Phase 7 and Phase 8 even though Phase 7 (badge) only needs the `IdentificationResult` source field, not the camera ViewModel changes. My §4 (badge) and §5 (pickers) can be developed in either order once §3.1 lands.

---

## 2. GEMINI (`PLANTPOTTING-0003-GEMINI.md`)

### 2.1 What is stronger than mine

- **§4.5 Performance & Hardware Acceleration phase** — Gemini has a whole phase for `GpuDelegate`, `numThreads` matched to big cores, and thermal-throttling measurement on a burst of 10 inferences. I make all of that a §3.2 nice-to-have or skip it. For a real device user-flow, GPU delegation often is the difference between "≤2.5 s" and "ANR risk". Gemini takes performance more seriously than I do.
- **§7.2 ABI-filter mitigation for APK size** — explicit `ndk.abiFilters` to `arm64-v8a` + `x86_64`, stripping `armeabi-v7a` and `x86`. My §7.10 says "APK growth is unconstrained" and waves the issue away; Gemini at least proposes a concrete lever even if not strictly needed this sprint.
- **§3 (should-land) "Warm-up" the Interpreter during CameraScreen bind window** — calls back to PLANTPOTTING-0002's UX 1 bind window. This is a genuinely good UX integration insight that ties the ML latency to the existing camera lifecycle. My §7.2 mitigation just lazy-loads on app start; Gemini ties warm-up to the user's actual on-screen moment.
- **§4.4 `UnsureScreen` "I'm not sure, give me a safe bet" → `standard-houseplant`** — a single-tap escape hatch with a safe-default recipe. My pickers (low-confidence + archetype) require two taps to land somewhere. Gemini's CTA is a faster bail-out for users who don't want to read three Latin names.
- **§4.2 architecture diagram (Tier 1 / 2 / 3 funnel)** is more readable than my §4.3 threshold table. The three tiers map cleanly to UX states (direct result / generic-match result / unsure picker).
- **§7.3 named the TFLite Interpreter init ANR risk** explicitly. CODEX and I both lazy-load but neither calls out the cold-start ANR risk by name.

### 2.2 What is weaker than mine

- **Model choice is hand-wavy.** "MobileNetV3-Small (iNaturalist)" with no TF Hub URL, no exact label count (says "thousands" / "~10,000 species" without source), no exact file size ("~15-30 MB"), and an unsourced ">85% on our 16 targets in good light" claim. My §4.1 names the exact TF Hub model id (`google/aiy/vision/classifier/plants_V1/3`), variant (FP16), file size (25.4 MB), label count (~2,101), license (Apache-2.0), and Google's published top-1 (67%) / top-5 (84%) numbers.
- **Section numbering is broken.** Two phases labelled "Phase 4" (Phase 4 + Phase 4.5), and the narrative jumps from "Phase 2" to "Phase 4" with no Phase 3 header even though Phase 4's tasks are numbered §3.1, §3.2, §3.5 (off-by-one with the header). This will trip the sprint-execute skill's task-tick logic.
- **§8 acceptance criteria are pre-checked `- [x]`** in the draft. They should be `- [ ]` until the sprint is run. This is a draft mistake that will short-circuit acceptance.
- **§4.2 `IdentificationResult` gains `confidence: Float` directly** without a backwards-compatibility note for `StubPlantIdentifier`'s existing test callers. My §3.1 makes the new fields default-valued so the stub callers compile unchanged.
- **§4.5 "Generic Match" Tier 2** silently shows the archetype recipe under a "General [Genus]" title. This is exactly what CODEX's §5.2 risk warns against — a confident-looking recommendation from a weak match. Gemini ships this as Tier 2; CODEX explicitly forbids it; I route Tier-2-equivalents through the picker instead. Gemini's design is the most user-friendly *and* the most likely to give wrong potting advice from a misclassified plant.
- **No `verifyNoNetworking` regression test.** §2.4 just runs the existing gate; it doesn't add a JVM test that asserts `okhttp`/`retrofit`/`firebase` are absent from the runtime classpath. My §1.3 adds that as a RED-first task so future PRs can't silently introduce a transitive networking dep.
- **No retroactive PLANTPOTTING-0002 §310 tick.** Same gap as CODEX. My §7.3 closes the loop.
- **§0.1 RED-first task is "boot the emulator and confirm the script fails"** — that isn't TDD. It's reproducing the bug; the test that drives the fix is the script-shim harness in my §6.2. Gemini's Phase 0 starts on the wrong foot.
- **No `check-stub-isolation.sh` invariant explicitly held throughout the sprint.** §3.6 runs it once after wiring; my §6 hard-gate makes it a sprint-blocker for any task that adds a new reference outside `identify/`.
- **Top-3 candidates aren't stored on the result** — §3.2 only adds `confidence: Float`. The `UnsureScreen` (§4.4) needs the top-3 list but the data class doesn't carry it; the implementation has to plumb it via a different channel that the draft doesn't specify. My §3.1 adds `topCandidates: List<Candidate>` for exactly this reason.
- **§3.5 `@TestOnly` annotation** isn't enforceable in Kotlin; it's a JetBrains hint, not a compile-time gate. My §4.4's seam-grep + `check-stub-isolation.sh` is the actual gate.
- **§4.5.x Performance phase risks scope-balloon for a 2-week sprint.** GPU delegate + driver-incompat fallback + thermal measurement on physical hardware is at least 2–3 days of work and Gemini puts it in must-land-adjacent (§4.5, not §3.2). My §3.2 keeps perf as a nice-to-have so the sprint actually finishes.

### 2.3 What tasks are missing

- **No `model_manifest.json` with hash.** Gemini ships labels + mapping + model but no manifest with model integrity hash, source URL, license attribution. CODEX's §2.6 is the right shape; mine has the same gap (only LICENSE attribution under §4.5).
- **No `PlantIdentifierContractTest`** to pin the seam shape (CODEX §0.4 has this; I rely on prose; Gemini has nothing).
- **No mapping-content validation test on every push.** §1.3 `ModelMappingTest` validates JSON parseability and KB-id existence but doesn't validate inverse coverage (every KB species reachable). Mine §7.4 names this gap; CODEX's §3.9 actually ships it.
- **No fixture-set with deterministic golden expected ids beyond the 3 species** in §3.3 `IdentificationAccuracyTest`. My §3.2 has `monstera`, `ficus-lyrata`, `blank-grey`, `noise` — explicitly including the *negative* fixtures (blank, noise) that prove low-confidence routing.
- **No Bug A regression-test scaffold under `scripts/test-integration-flow-mocks/`** with adb-shim PowerShell scripts. Gemini's §0.1 just reproduces the bug on a real emulator; mine §6.2 ships a deterministic shim test.
- **No "see other candidates" disclosure on the high-confidence path.** Gemini's UnsureScreen only fires below 0.6, so a 0.85-confidence wrong answer has no escape hatch.
- **No editorial mapping notes file** akin to `plant-substrate-kb-notes.md`.
- **No `IdentifyModule` rename / relocation task.** Same gap as CODEX.

### 2.4 What risks are underweighted

- **Only 4 risks named.** Mine has 11; CODEX has 9. Specifically missing:
  - **Stub isolation regression** as the Hilt swap moves around.
  - **Hilt test-binding combo burn** (`@TestInstallIn` + Compose + CameraX + TFLite).
  - **Mapping staleness when KB grows.**
  - **TFLite native ABI mismatch on GMD.**
  - **`verifyNoNetworking` false positive on `play-services-tasks` transitive.**
  - **Test-config sandbox overlay on Windows.**
- **§7.1 "Model accuracy in poor light"** treats the 0.6 threshold as the "primary defense." It's actually a knob — the real defense is the manual picker's existence + the safe-default fallback. CODEX's §5.2 frames this more honestly.
- **§7.4 "Mapping Drift"** mentions iNaturalist label-vs-common-name mismatch but not the harder problem: model labels at *genus* rank when the KB needs *species* (e.g., `Hoya` → which `Hoya`?). My §7.1 + CODEX's §3.5 alias logic both address this; Gemini doesn't.
- **No risk for the `confidence: Float` field being a backwards-incompatible change** to `IdentificationResult` for stub-test callers.

### 2.5 What sequencing is wrong

- **Numbering is broken** (two Phase 4s, off-by-one task numbers in Phase 4) — this will break sprint-execute's task-tick logic, since the skill counts `- [ ]` boxes by section.
- **Linear waterfall ASCII diagram** with no parallelism. Bug A (Phase 0) is presented as blocking everything else even though it touches only `scripts/`. My §6 / CODEX's §4.2 both note Bug A is parallelisable; Gemini doesn't.
- **§4.5 (Performance) lands before §5 (Acceptance)** as if it were must-land scope. Performance work this deep should either be must-land *with explicit acceptance metrics* (e.g., "<800 ms inference time" — Gemini does name this in §8 acceptance, fine) or be a §3.2 nice-to-have. Splitting the difference puts the sprint at risk of the implementer spending 3 days on GPU delegate work and missing the manual picker.
- **§5.3 expects the integration-flow expected manifest to change** because the badge text changed. That's a real concern — but the task list doesn't include "regenerate `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`" as an actual edit. So the integration script will diff-fail on the badge change and Gemini's draft has no task to update the expected artifact.
- **§4.6 `CameraViewModel` confidence-threshold check lives in the ViewModel** (not in the identifier or a mapper). That couples the threshold policy to the UI layer; CODEX's `ModelScoreMapper` and my §4.3 keep the threshold in the identifier so unit tests can cover the policy without ViewModel scaffolding.

---

## 3. If I were merging, I'd keep…

1. **CODEX §0.4 `PlantIdentifierContractTest`** — Phase 0 RED test that pins the seam shape. Strictly better than my prose-only §4.4 invariant.
2. **CODEX §2.6 + §2.7 `model_manifest.json` with hash + `ModelManifestTest`** — make the model artifact tamper-evident. Carry the manifest hash into my acceptance criteria.
3. **CODEX §3.9 "all 16 KB species reachable" golden test** — ship the inverse coverage check this sprint instead of deferring it to PLANTPOTTING-0004 like I did in §7.4.
4. **CODEX §4.4 `InterpreterFacade`** — clean unit-test seam over native TFLite. Replaces the messier "construct fixture `Classifications` directly" approach in my §3.4.
5. **CODEX §9 `docs/ml/model-card-aiy-plants-v1.md`** — alongside (not instead of) my per-row `ml-mapping-notes.md`. The model card is the recognised industry artifact and belongs next to the `.tflite`.
6. **GEMINI §3 (should-land) "Warm-up Interpreter during CameraScreen bind window"** — concrete latency-hiding lever tied to the existing camera lifecycle, better than my "lazy-load on app start" mitigation in §7.2.
7. **GEMINI §4.4 "I'm not sure, give me a safe bet" CTA** — single-tap escape from the picker to a safe-default `standard-houseplant`-style recipe. Worth folding into my §5.2 `ArchetypePickerScreen` as a footer action.

From my own draft I'd keep: pinned model version + variant + measured size (§4.1), single-axis threshold table (§4.3), three-transcript Bug A evidence structure (§6.5), `Wait-ForNode` budget reasoning (§6.4), retroactive PLANTPOTTING-0002 §310 tick (§7.3), and the full risk register (§7) including ABI / test-config / sandbox-overlay risks that the other two drafts omit.

I would *drop* CODEX's `LowConfidenceIdentificationException` in favour of my `lowConfidence: Boolean` + `topCandidates: List<Candidate>` on `IdentificationResult` — keeps the seam contract honest and avoids exception-as-control-flow. And I would *not* adopt GEMINI's Tier-2 "Generic Match shows archetype recipe under a 'General [Genus]' title" — that ships a confident-looking recommendation from a weak match, which is exactly the failure mode CODEX §5.2 calls out.
