---
through_sid: PLANTPOTTING-0013
---

# Feature ideas — async funnel

The principal's async feature-idea inbox. Raw thoughts land at the top, get fleshed out,
then are accepted (→ a sprint or the roadmap) or declined. The `/sprint-planner` and
`/roadmap` skills read this file before their interview and offer to fold open items in —
they never accept, develop, or move an entry unprompted.

`through_sid` tracks how far this backlog has been reconciled into sprints.

## Raw ideas

_(none open — monetisation moved to `idea decisions → accepted` below: folded into the roadmap as a deferred Known Gap + V3 milestone sketch, gated behind the free Launch.)_

## developed idea

_(none open — the Play Store publishing/licensing idea was accepted into the roadmap; see `idea decisions → accepted` below.)_

## idea decisions

### accepted and added to roadmap or sprint

- **Accepted 2026-06-09 (`/roadmap refresh` through 0013) — Launch track: publish to the Google Play Store + license the source (PolyForm Noncommercial).** Folded into `docs/ROADMAP.md`: a new **Launch — Google Play Store public release** milestone (parallel to V1, does not block it), a detailed **`PLANTPOTTING-0014 — release readiness`** Next block (code/config track + Play-process track), two Layer-Status rows (Licensing & legal; Release engineering / signing / distribution), two Known Gaps (★ not release-ready: targetSdk 34 < Play min 35 / no signing / debug-APK-only / placeholder icon / R8 off; and no project LICENSE / no privacy policy), and the targetSdk 34→35 standing-lock lift. Principal decisions: **public repo, PolyForm Noncommercial 1.0.0**; **Personal** Play account (12-tester / 14-day closed-test gate applies); **free launch**. Licensing already commercial-clean (both ML models Apache-2.0; all 44 reference photos Unsplash/Pexels/CC0; no copyleft; `verifyNoNetworking` holds). Full write-up: [`play-store-publishing.md`](play-store-publishing.md). _Next step: `/sprint-planner` on PLANTPOTTING-0014 once this refresh PR merges._

- **Accepted 2026-06-09 (`/roadmap refresh` through 0013) — monetisation spike (DEFERRED, recorded only).** Raw idea #1 (40 ideas → top-5 easiest + top-5 biggest-profit → 2×2 ease-vs-revenue scoring; levers: in-app ads, paid/pro Play tier, subscription, substrate-supplier affiliate, vertical integration, horizontal referral partnerships) was recorded in the roadmap as **Known Gap 7** plus a **V3 — Sustainable product / monetisation** milestone sketch. **Not scoped as a sprint** — it is gated behind shipping the free Launch (0014). Revisit for `/sprint-planner` only after the free Play release is live.

### declined

_(none yet — include a one-line reason when declining)_


## DONE
(Ideas that have been actiontioned should be moved here. Should be prefixed with the sprint they were done in e.g. **Done-[sprint name]**)

- **Done-[no sprint; drafter-tooling infra, 2026-06-08] — codex↔OpenRouter Pattern-B fallback drafter
  (DeepSeek V4 Pro).** The developed idea, wired + tested + evaluated + integrated into the skills. Shipped:
  `[model_providers.openrouter]` block in `~/.codex/config.toml` with **`wire_api = "responses"`** (the
  entry's original `"chat"` is dead on codex 0.137+; OpenRouter exposes `/responses`); verified via smoke +
  repo-grounding + file-write tests (0 hallucinated paths); a 5-candidate draft+critique road-test
  ([`alt-drafter-models-eval/round2/EVALUATION.md`](alt-drafter-models-eval/round2/EVALUATION.md)) confirming
  **DeepSeek V4 Pro** as the reliable repo-grounded drafter (free models fail the agentic write — usable only
  as critiquers; `qwen3-coder:free` unusable today); wired into **`sprint-planner`** (drafts + critiques) and
  **`roadmap`** (INIT + REFRESH) as the **codex/agy fallback** — fills the third slot only when codex or agy
  fails, never replacing claude, capped at one substitution. How-to:
  [`codex-openrouter-agentic-setup.md`](codex-openrouter-agentic-setup.md). _(Qwen 3.7 Max was scoped but
  DeepSeek won on cost+reliability; Qwen-Max remains an option for a stronger-but-pricier drafter.)_

- **Done-PLANTPOTTING-0010 = combined app-experience sprint (both open raw ideas folded in).** Raw idea #1
  (*strong-confidence match with no KB entry → surface it + a wireframe "add this plant" button that
  totals requests for later*) became **Pillar B** of the sprint. Raw idea #2 (*UI/UX improvements:
  pickable clean/elegant theme, confidence number + progress bar, a "My Plants" folder, species list
  contained within the search-species control, a reference picture per plant*) became **Pillar A**.
  A text-only KB expansion over the remaining unmapped model classes is **Pillar C**. All three sit
  behind the frozen `PlantIdentifier` seam and the network-free gate; the pothos↔Pilea boundary fix
  (the roadmap's prior Next) is deliberately **deferred** and Pilea stays unmapped. Theme delivery is
  2–3 candidate Compose themes shipped in a debug APK + screenshots so the principal picks from real
  renders (Claude can't produce polished design images). _Accepted 2026-06-05 (`/sprint-planner`):
  plan at `docs/sprints/PLANTPOTTING-0010.md`._

- **Done-PLANTPOTTING-0008 (folded-in UX bug) = shutter button greys out after back-nav from results.**
  Raw idea *"taking picture then going back from results screen causes the 'take picture' button to
  grey out"* pulled into PLANTPOTTING-0008 (the training-data availability spike) as the sprint's one
  code change. Root-caused during planning: the back-stack `CameraViewModel` is left at
  `CameraUiState.Success` after a capture, and the shutter enables only on `Idle`/`Failure`
  (`CameraScreen.kt:191`) — fix resets to `Idle` on return via the existing `viewModel.reset()`.
  _Accepted 2026-06-05 (`/sprint-planner`): folded into `docs/sprints/PLANTPOTTING-0008.md` Phase 3._

- **Done-PLANTPOTTING-0007 = houseplant model swap (V1 entry).** Raw idea *"the plant ML is too
  limited on houseplants — this needs to get MUCH better sooner"* folded directly into the
  sprint. 0007 surveys on-device, network-free, houseplant-weighted candidate classifiers
  (PlantNet-300K MobileNetV3-Small/EfficientNet-Lite INT8, iNat on-device plants, PlantCLEF
  stretch); builds a swap-evaluation harness over an expanded ≥6-species real-photo fixture set
  (Monstera + jade + snake plant, pothos, ZZ, peace lily, +2) comparing top-1/top-3, in-vocab
  coverage, size, and inference latency against the AIY baseline; and **lands a running
  prototype** of the winner wired behind a single `ACTIVE_MODEL_ROOT` switch, identifying plants
  live through `OnDevicePlantIdentifier`. No in-sprint ML training (a fine-tuning sprint is the
  documented fallback if no public model wins). _Accepted 2026-06-04 (`/sprint-planner`): SID
  reserved, plan at `docs/sprints/PLANTPOTTING-0007.md`._

- **Done-PLANTPOTTING-0006 = multi-species calibration sweep + 2 UX fixes.** With 0005 fully
  closed (real-photo probe confirmed `monstera-deliciosa @ 0.8984`, high-conf, no seeding
  needed), 0006 is no longer "finish 0005" — it's narrower: (1) extend the probe to the
  other in-vocab species (`crassula-ovata`) so `perSpeciesThresholds` gains real
  evidence across ≥2 species; (2) fix the two 0005-review UX findings in
  `docs/sprints/feedback/PLANTPOTTING-0005/feedback.md` — `LowConfidencePicker` subtitle
  leaks "model" jargon, and candidate chips render "(0%)" on degenerate captures.
  _Accepted 2026-06-04 (`/roadmap refresh`): folded into ROADMAP Proposed Sprint Path
  (Next: PLANTPOTTING-0006) and Known gaps._