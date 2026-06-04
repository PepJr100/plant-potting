---
last_updated: 2026-06-04
through_sid: PLANTPOTTING-0005
---

# ROADMAP

One-page snapshot of where PlantPotting stands and what's next. Sprint history lives
in [`docs/sprints/`](sprints/); this file is the current view.

> Maintained by the `/roadmap` skill (INIT / REFRESH / BUMP). The frontmatter above is
> parsed by routing — `last_updated` and `through_sid` drive when a REFRESH is due.

## Current state (post-PLANTPOTTING-0005)

On-device ML identifies plants end-to-end via the AIY Plants V1/3 UINT8 TFLite model;
the post-shutter UX is now polished on both the high-confidence and the (more common)
low-confidence paths, and the `CameraUiState.Failure` state surfaces a persistent
bottom-anchored banner with a `Try again` retry. The test infrastructure has caught up
with PLANTPOTTING-0003's architectural debt — the global `TestIdentifyModule` is gone,
all seven dependent instrumentation tests use per-test `@BindValue`, and the previously
`@Ignore`'d `PermissionDeniedFlowTest` is reachable via `FakeCameraPermissionGuard`.
Confidence calibration is partially in place: `ModelManifest.perSpeciesThresholds` and
`ModelScoreMapper`'s per-class override consultation ship, but the real-photo probe
(§5.5) + accuracy-bearing assertion (§5.6) + optional seeding (§5.7) remain deferred
pending a CC-licensed Monstera fixture and a GMD probe run.

## Layer status

| Layer | Status | Notes |
| --- | --- | --- |
| KB (species.json, archetypes.json, plant_class_map.json) | ✓ shipped | 16 species, 18 mapping entries (2 overlap AIY V1 vocab). Locked. |
| Identifier (interface + on-device impl + stub) | ✓ shipped | `OnDevicePlantIdentifier` is production. Seam unchanged since 0003 §4.4. |
| Confidence calibration | △ partial | `perSpeciesThresholds` mechanism in place (manifest, reader, mapper); map ships empty pending probe evidence. Real Monstera fixture + accuracy assertion pending (PLANTPOTTING-0005 §5.4–§5.8). |
| Camera UI | ✓ shipped | Shutter, bind-pending overlay, Failure banner with `Try again`. |
| Result + Recommendation UI | ✓ shipped | Source-driven badge; archetype + recipe; sum-to-100 enforced. |
| LowConfidencePicker | ✓ shipped | Subtitle + chevron chips + empty-state cards + outlined archetype CTA + small-screen viewport assertion. |
| Permission flow | ✓ shipped | Settings round-trip recovery; permanent-denial test reachable via fake guard hook. |
| Instrumentation tests | ✓ shipped | All 7 migrated to `@BindValue`; `@Ignore` count is 0; `LowConfidenceFlowTest` covers chip + search paths. |
| Stub-isolation gate | ✓ shipped | `scripts/check-stub-isolation.sh` GREEN. |
| Network-free gate | ✓ shipped | `verifyNoNetworking` GREEN. |
| Integration manifest | ✓ shipped | `scripts/integration-flow.ps1` (cold + warm + buildonly) diffs against `expected-artifacts/PLANTPOTTING-0001*.txt`. No re-baselining in 0005. |
| Documentation | △ partial | This file + `ml-mapping-notes.md` calibration entry land in 0005; per-sprint results doc + probe data pending. |

## Species/Model Coverage

How much of the bundled KB the on-device model identifies verbatim, and the calibration state.

| Axis | State | Notes |
| --- | --- | --- |
| KB species | 16 | `species.json`; locked. |
| In-vocab vs AIY V1/3 | 2 of 16 | Only *Monstera deliciosa* and *Crassula ovata* overlap the AIY Plants V1/3 vocabulary verbatim (`_comment_coverage`). |
| Routing | threshold-gated | Top-1 score vs per-class threshold in `model_manifest.json` → `ResultScreen` (high-conf) or `LowConfidencePicker` (low-conf / out-of-vocab). |
| Calibration | △ emerging | `perSpeciesThresholds` mechanism ships; the map is empty pending the real-photo probe (§5.5) + accuracy assertion (§5.6). Promotes from `emerging` once thresholds gain probe-backed evidence across ≥2 species. |

## Sprint history

| SID | Title | Status | Headline outcome |
| --- | --- | --- | --- |
| PLANTPOTTING-0001 | Android scaffold + end-to-end stub flow with KB | done | Camera → result → recommendation flow on stub identifier; KB + Hilt + Compose scaffolding; integration-flow manifest baseline. |
| PLANTPOTTING-0002 | Fix sprint for PLANTPOTTING-0001 review bugs | done | Settings round-trip recovery (Bug 4); permission rationale wording; bind-pending overlay. |
| PLANTPOTTING-0003 | On-device ML identifier + Bug A fix + source-driven badge | done | `OnDevicePlantIdentifier` wired to AIY V1/3; source-driven `ResultScreen` badge; `LowConfidencePicker` v1; `_comment_coverage` (2 of 16 species in-vocab). |
| PLANTPOTTING-0004 | Fix sprint for PLANTPOTTING-0003 review bugs | done | UINT8 preprocessor branch (Bug 1); `testTagsAsResourceId` bridge (Bug 2); `OnDeviceModelRealInterpreterTest` lands. |
| PLANTPOTTING-0005 | Post-shutter polish + un-defer carry-forward from 0003/0004 | in-progress | LowConfidencePicker polished; bottom-anchored Failure banner; `TestIdentifyModule` deleted (7 tests migrated to `@BindValue`); `LowConfidenceFlowTest` + un-`@Ignore`'d PermissionDenied; `perSpeciesThresholds` mechanism. Real-photo probe + GMD gates carry to 0006. |

## Known gaps

Carried forward from 0005's non-goals (§2.4) and Blockers section:

- **Model swap.** AIY V1/3 stays. Only 2 of 16 KB species are in-vocab verbatim; calibration limits accepted.
- **Delegate / quantization variants.** No INT8 / GPU / NNAPI delegate yet.
- **No net-new screens.** No `CaptureFailedScreen`, `Settings`, or `ModelInfoScreen`.
- **No ML training or retraining.**
- **Single-species fixture.** Real Monstera photo is the start; multi-species fixture sweep is a future sprint.
- **`PlantIdentifier` / `IdentificationResult` interface changes.** Frozen per 0003 §4.4.
- **KB edits.** Locked.
- **AGP / Kotlin / Compose / Hilt / TFLite version bumps.** Locked.
- **README rewrite.** Append-only; link this roadmap instead.
- **`expected-artifacts` re-baselining.** Off the table.

Specific items deferred from 0005's Blockers (in `docs/sprints/PLANTPOTTING-0005.md`):

- §5.4 — source + bundle a CC-licensed Monstera deliciosa photograph.
- §5.5 — run the on-device probe and record top-1 / top-3 / score / route.
- §5.6 — pick the accuracy-bearing assertion (preferred vs fallback) based on §5.5.
- §5.7 — conditionally seed `perSpeciesThresholds` for at most one species, gated on §5.5.
- §3.12 / §4.7 / §7.2 / §7.6 — GMD instrumentation runs + manual emulator walkthrough.
- §7.3 — `integration-flow.ps1` cold + warm + buildonly transcripts.

## Proposed Sprint Path

### Active horizon (detailed)

#### Next: PLANTPOTTING-0006 — Finish-0005 + multi-species calibration sweep
- **Intent:** Land the deferred accuracy work from 0005 and extend it into a small
  multi-species calibration sweep, so per-species thresholds gain real probe-backed
  evidence rather than shipping an empty map.
- **Entry conditions:** 0005 reviewed and merged to `origin/main`; a CC-licensed
  *Monstera deliciosa* photograph sourced for the fixture.
- **Scope hints:**
  - Land the real Monstera fixture + the §5.5 on-device probe + the §5.6 accuracy
    assertion + any §5.7 seeding the probe justifies.
  - Extend to a small multi-species fixture sweep (*Crassula ovata* at minimum — the only
    other in-vocab species per `_comment_coverage`) and seed `perSpeciesThresholds` for any
    species the data demands.
  - Decide whether to pick up the deferred GMD / emulator-walkthrough gates from 0005 or
    split them off, based on how quickly fixture sourcing lands.
- **Milestone contribution:** advances **V0.1 — Trustworthy confidence calibration**.
- **Note:** scope is intentionally narrow — accuracy work is expensive in evidence and
  shouldn't sprawl.

### Milestone ladder (skeleton — sprints become detailed as they approach)

#### MVP — On-device ID + KB-driven potting-mix recommendation — *(shipped ~PLANTPOTTING-0003)*
Camera → on-device identification → routing decision → KB-driven recipe, network-free,
for the bundled species set.

#### V0.1 — Trustworthy confidence calibration *(in progress)*
- **Exit criteria:** per-species thresholds backed by a real-photo probe; an
  accuracy-bearing assertion in CI; a multi-species fixture sweep (not just one species).
- **Skeleton sprints:** PLANTPOTTING-0006 (above); follow-on fixture/threshold sweeps TBD.

#### V1 — Broad species coverage
- **Exit criteria:** most common houseplants identify directly (model swap, supplemental
  classifier, or expanded vocab) so the low-confidence path is the exception, not the rule.
- **Skeleton sprints:** one-liners (rough) — concretised when V0.1 nears exit.

#### V2 — Richer care guidance
- **Exit criteria:** beyond the one-shot recipe — repotting schedule, care reminders, or
  personalization to the user's plant set.
- **Skeleton sprints:** one-liners (rough).

#### Beyond V2 — Plant-health diagnostics
- Identify stress/pest/over-watering signs from the same photo pipeline (sketch only).

## Changelog

- 2026-06-04 — added roadmap-skill frontmatter (`last_updated`, `through_sid`), a
  Species/Model Coverage section, and reshaped "Candidate next sprint" into a Proposed
  Sprint Path (active horizon + milestone ladder). Content otherwise unchanged; no sprint
  window reconciled. First entry — the `/roadmap` skill appends one per REFRESH hereafter.
