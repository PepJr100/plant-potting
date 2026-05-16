# ROADMAP

One-page snapshot of where PlantPotting stands and what's next. Sprint history lives
in [`docs/sprints/`](sprints/); this file is the current view.

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

## Candidate next sprint

**PLANTPOTTING-0006** is shaping up as a *finish-0005 + multi-species calibration sweep*
sprint. Concretely: (a) land the real Monstera fixture + the §5.5 probe + the §5.6
accuracy assertion + any §5.7 seeding the probe justifies; (b) extend the calibration
work to a small multi-species fixture sweep (Crassula ovata at minimum, since it's the
only other in-vocab species per `_comment_coverage`) and seed `perSpeciesThresholds`
entries for any species the data demands. Whether 0006 picks up the deferred GMD /
emulator-walkthrough verification gates from 0005 or splits those off depends on how
quickly the fixture sourcing lands. Scope is intentionally narrow — accuracy work is
expensive in evidence and shouldn't sprawl.
