---
through_sid: PLANTPOTTING-0010
---

# Feature ideas — async funnel

The principal's async feature-idea inbox. Raw thoughts land at the top, get fleshed out,
then are accepted (→ a sprint or the roadmap) or declined. The `/sprint-planner` and
`/roadmap` skills read this file before their interview and offer to fold open items in —
they never accept, develop, or move an entry unprompted.

`through_sid` tracks how far this backlog has been reconciled into sprints.

## Raw ideas

- would like a spike on monetisation ideas. Obvious idea are 1. Targeted ads in app 2. small cost on play store (for pro / no adds version) 3. subscription (for pro / no adds version) 4. link-up with substrate supplier (vereral revenue) 5 . vertical intergration (sell substrate myself) 6. Horizontal partnerships. e.g. plantpots / plant supplies through referal links
  - would like to idea other ideas (think of 40 refine to top 5 "Easiest to implement" top 5 "Biggest potential profit return")
  - score the two "top 5" and my ideas on ease to impliment / potential revenue 2x2 

## developed idea

- **Draft a codex `config.toml` OpenRouter profile (DeepSeek V4 Pro / Qwen 3.7 Max) so Pattern-B is ready
  to test once the $10 credit lands.** Add a `[model_providers.openrouter]` block to `~/.codex/config.toml`
  (`base_url = "https://openrouter.ai/api/v1"`, `wire_api = "chat"`, `env_key`, `requires_openai_auth = false`)
  and a documented invocation (`codex --config model_provider=openrouter --config model=deepseek/deepseek-v4-pro …`
  / `…=qwen/qwen3.7-max …`). Goal: a repo-grounded **agentic** drafter/critiquer (Pattern B) backed by a paid
  OpenRouter model — the agy-quota fallback that does *not* hallucinate file paths. Spend belongs on Pattern-B
  drafting only, not Pattern-A. Full rationale, pricing, reliability findings, and the config snippet are in
  [`alt-drafter-models-eval.md`](alt-drafter-models-eval.md) §7B (agentic CLI backend) + §7C (paid-tier option).
  Not yet wired into the `sprint-*` skills — `.claude/skills/` is gitignored, so this is a manual config on the
  principal's machine + a future skill update. _Awaiting the OpenRouter credit before testing._

## idea decisions

### accepted and added to roadmap or sprint

- **PLANTPOTTING-0010 = combined app-experience sprint (both open raw ideas folded in).** Raw idea #1
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

- **PLANTPOTTING-0008 (folded-in UX bug) = shutter button greys out after back-nav from results.**
  Raw idea *"taking picture then going back from results screen causes the 'take picture' button to
  grey out"* pulled into PLANTPOTTING-0008 (the training-data availability spike) as the sprint's one
  code change. Root-caused during planning: the back-stack `CameraViewModel` is left at
  `CameraUiState.Success` after a capture, and the shutter enables only on `Idle`/`Failure`
  (`CameraScreen.kt:191`) — fix resets to `Idle` on return via the existing `viewModel.reset()`.
  _Accepted 2026-06-05 (`/sprint-planner`): folded into `docs/sprints/PLANTPOTTING-0008.md` Phase 3._

- **PLANTPOTTING-0007 = houseplant model swap (V1 entry).** Raw idea *"the plant ML is too
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

- **PLANTPOTTING-0006 = multi-species calibration sweep + 2 UX fixes.** With 0005 fully
  closed (real-photo probe confirmed `monstera-deliciosa @ 0.8984`, high-conf, no seeding
  needed), 0006 is no longer "finish 0005" — it's narrower: (1) extend the probe to the
  other in-vocab species (`crassula-ovata`) so `perSpeciesThresholds` gains real
  evidence across ≥2 species; (2) fix the two 0005-review UX findings in
  `docs/sprints/feedback/PLANTPOTTING-0005/feedback.md` — `LowConfidencePicker` subtitle
  leaks "model" jargon, and candidate chips render "(0%)" on degenerate captures.
  _Accepted 2026-06-04 (`/roadmap refresh`): folded into ROADMAP Proposed Sprint Path
  (Next: PLANTPOTTING-0006) and Known gaps. Awaits `sprint-planner` to reserve the SID._

### declined

_(none yet — include a one-line reason when declining)_
