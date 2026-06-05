---
through_sid: PLANTPOTTING-0008
---

# Feature ideas — async funnel

The principal's async feature-idea inbox. Raw thoughts land at the top, get fleshed out,
then are accepted (→ a sprint or the roadmap) or declined. The `/sprint-planner` and
`/roadmap` skills read this file before their interview and offer to fold open items in —
they never accept, develop, or move an entry unprompted.

`through_sid` tracks how far this backlog has been reconciled into sprints.

## Raw ideas

_(none open — last raw idea folded into PLANTPOTTING-0008)_

## developed idea

_(none yet — raw ideas that have been fleshed out into a paragraph go here)_

## idea decisions

### accepted and added to roadmap or sprint

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
