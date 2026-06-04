---
last_updated: 2026-06-04
through_sid: PLANTPOTTING-0006
---

# ROADMAP

One-page snapshot of where PlantPotting stands and what's next. Sprint history lives
in [`docs/sprints/`](sprints/); this file is the current view.

> Maintained by the `/roadmap` skill (INIT / REFRESH / BUMP). The frontmatter above is
> parsed by routing — `last_updated` and `through_sid` drive when a REFRESH is due.

## Current state (post-PLANTPOTTING-0006)

On-device ML identifies plants end-to-end via the AIY Plants V1/3 UINT8 TFLite model;
the post-shutter UX is polished on both the high-confidence and the (more common)
low-confidence paths, and the `CameraUiState.Failure` state surfaces a persistent
bottom-anchored banner with a `Try again` retry. The test infrastructure has caught up
with PLANTPOTTING-0003's architectural debt — the global `TestIdentifyModule` is gone,
all seven dependent instrumentation tests use per-test `@BindValue`, and the previously
`@Ignore`'d `PermissionDeniedFlowTest` is reachable via `FakeCameraPermissionGuard`.
**The V0.1 multi-species calibration sweep is complete:** the
`ModelManifest.perSpeciesThresholds` mechanism (manifest + reader + `ModelScoreMapper`
override-then-global) ships and is now probe-backed across **both** in-vocab species. A
CC-BY-SA *Monstera deliciosa* photo probes @ **0.8984**, `lowConfidence=false` (high-conf);
a CC0 *Crassula ovata* (jade) photo probes @ **0.1055**, `lowConfidence=true` — jade ranks
top among *mapped* candidates but routes low-confidence, so an **honest fallback** accuracy
assertion (not the strong form) guards it in CI. `perSpeciesThresholds` ships **empty by
design**: a 0.1055→0.55 gap is not cleanly closeable by a per-class override, so no seeding
was warranted. Both 0005-review UX fixes also landed — the low-conf subtitle is de-jargoned
and floored-to-zero candidate chips drop the `(0%)` suffix while staying selectable. The
`integration-flow.ps1` cold + warm + buildonly transcripts are all GREEN. PLANTPOTTING-0006
is closed.

**The sweep's result is the headline finding:** the AIY Plants V1/3 model is tuned for
broad/wild flora, not houseplants. Jade — a ubiquitous houseplant — scores only ~10.5%
confidence on a clean canonical fixture, and only 2 of 16 KB species are in-vocab at all.
Calibration is mechanically sound but bottlenecked on the model's ceiling. A houseplant-tuned
**model swap is now the next-up milestone (V1)**, elevated from "later" by 0006's evidence and
the user's review feedback.

_Derived stats: ~8,455 Kotlin LOC; 38 unit + 8 instrumentation test files; acceptance
checkboxes 367/410 ticked (~90%) across `docs/sprints/*.md` (the open boxes are the 0005
manual-GMD-walkthrough items the user accepted code + JVM coverage in lieu of)._

## Layer status

| Layer | Status | Notes |
| --- | --- | --- |
| KB (species.json, archetypes.json, plant_class_map.json) | ✓ shipped | 16 species, 18 mapping entries (2 overlap AIY V1 vocab). Locked. |
| Identifier (interface + on-device impl + stub) | ✓ shipped | `OnDevicePlantIdentifier` is production. Seam unchanged since 0003 §4.4. |
| Confidence calibration | ✓ sweep complete | Mechanism shipped (`perSpeciesThresholds` in manifest, reader, mapper). Probe-backed across **2 of 2** in-vocab species — *Monstera deliciosa* @ 0.8984 (high-conf, strong assertion) and *Crassula ovata* @ 0.1055 (low-conf, honest-fallback assertion). Map empty by design — neither probe warranted an override. V0.1 sweep complete. **The data shows the model, not the calibration, is the limiting factor (see Known gaps → model swap).** |
| Camera UI | ✓ shipped | Shutter, bind-pending overlay, Failure banner with `Try again`. |
| Result + Recommendation UI | ✓ shipped | Source-driven badge; archetype + recipe; sum-to-100 enforced. |
| LowConfidencePicker | ✓ shipped | Subtitle (de-jargoned in 0006) + chevron chips (drop `(0%)` suffix on floored-zero candidates, still selectable) + empty-state cards + outlined archetype CTA + small-screen viewport assertion. |
| Permission flow | ✓ shipped | Settings round-trip recovery; permanent-denial test reachable via fake guard hook. |
| Instrumentation tests | ✓ shipped | All 7 migrated to `@BindValue`; `@Ignore` count is 0; `LowConfidenceFlowTest` covers chip + search paths. |
| Stub-isolation gate | ✓ shipped | `scripts/check-stub-isolation.sh` GREEN. |
| Network-free gate | ✓ shipped | `verifyNoNetworking` GREEN. |
| Integration manifest | ✓ shipped | `scripts/integration-flow.ps1` cold + warm + buildonly all GREEN against `expected-artifacts/PLANTPOTTING-0001*.txt`. No re-baselining in 0005; transcripts captured to `evidence/PLANTPOTTING-0005/`. |
| Documentation | ✓ shipped | This file + `ml-mapping-notes.md` calibration-provenance entry + `PLANTPOTTING-0005.md` results doc (probe numbers, §5.6/§5.7 decisions) all landed. |

## Species/Model Coverage

How much of the bundled KB the on-device model identifies verbatim, and the calibration state.

| Axis | State | Notes |
| --- | --- | --- |
| KB species | 16 | `species.json`; locked. |
| In-vocab vs AIY V1/3 | 2 of 16 | Unchanged — no model swap. Only *Monstera deliciosa* and *Crassula ovata* overlap the AIY Plants V1/3 vocabulary verbatim (`_comment_coverage`). |
| Routing | threshold-gated | Top-1 score vs per-class threshold (`perSpeciesThresholds`) falling back to the 0.55 global in `model_manifest.json` → `ResultScreen` (high-conf) or `LowConfidencePicker` (low-conf / out-of-vocab). |
| Calibration | ✓ probe-backed (2 of 2 in-vocab) | **Sweep complete this window.** Both in-vocab species now have real-photo, probe-backed results with accuracy assertions in CI: *Monstera deliciosa* @ 0.8984 (`lowConfidence=false`, strong form) and *Crassula ovata* @ 0.1055 (`lowConfidence=true`, honest fallback — jade is top *mapped* candidate but routes low-conf). `perSpeciesThresholds` is empty **by design** — neither probe warranted an override (a 0.1055→0.55 gap isn't cleanly closeable). V0.1 exit criteria met. The low jade score is the evidence motivating the V1 model swap — calibration is sound; the model's houseplant ceiling is the bottleneck. |

## Sprint history

| SID | Title | Status | Headline outcome |
| --- | --- | --- | --- |
| PLANTPOTTING-0001 | Android scaffold + end-to-end stub flow with KB | done | Camera → result → recommendation flow on stub identifier; KB + Hilt + Compose scaffolding; integration-flow manifest baseline. |
| PLANTPOTTING-0002 | Fix sprint for PLANTPOTTING-0001 review bugs | done | Settings round-trip recovery (Bug 4); permission rationale wording; bind-pending overlay. |
| PLANTPOTTING-0003 | On-device ML identifier + Bug A fix + source-driven badge | done | `OnDevicePlantIdentifier` wired to AIY V1/3; source-driven `ResultScreen` badge; `LowConfidencePicker` v1; `_comment_coverage` (2 of 16 species in-vocab). |
| PLANTPOTTING-0004 | Fix sprint for PLANTPOTTING-0003 review bugs | done | UINT8 preprocessor branch (Bug 1); `testTagsAsResourceId` bridge (Bug 2); `OnDeviceModelRealInterpreterTest` lands. |
| PLANTPOTTING-0005 | Post-shutter polish + un-defer carry-forward from 0003/0004 | done | LowConfidencePicker polished; bottom-anchored Failure banner; `TestIdentifyModule` deleted (7 tests migrated to `@BindValue`); `LowConfidenceFlowTest` + un-`@Ignore`'d PermissionDenied; `perSpeciesThresholds` mechanism + real-photo probe (*Monstera deliciosa* @ 0.8984, high-conf; map empty by design); accuracy assertion in CI; integration-flow cold/warm/buildonly GREEN. |
| PLANTPOTTING-0006 | Second in-vocab calibration probe (`crassula-ovata`) + two UX fixes | done | V0.1 multi-species sweep complete — *Crassula ovata* (jade) probes @ 0.1055 (low-conf), honest-fallback assertion in CI; `perSpeciesThresholds` stays empty by design (override not warranted). Two UX fixes: subtitle de-jargoned; `(0%)` chips drop the suffix, stay selectable. **Review surfaced the model-swap milestone — AIY V1/3 recognises wild flora, not houseplants.** |

## Known gaps

**Elevated this window — the model recognises wild flora, not houseplants (next milestone).**
0006's multi-species sweep is the evidence: *Crassula ovata* (jade), a ubiquitous houseplant,
scores only **0.1055** top-1 on a clean canonical fixture and routes low-confidence; only 2 of
16 KB species are in-vocab at all. The AIY Plants V1/3 model is tuned for broad/wild flora.
**Why it matters:** the app's core promise — identify the plant you're potting — is bottlenecked
on the model, not the (now-proven) calibration plumbing. The user's 0006 review feedback called
this out directly: *"the engine we have is recognising wildflowers… swap it out for something
that recognises houseplants better soon."* **Promoted from a "later" non-goal to the next-up
milestone (V1).** Captured in `feedback/PLANTPOTTING-0006/feedback.md`. **Likely sprint:**
PLANTPOTTING-0007 (model-selection spike).

Standing non-goals (carried from 0005's §2.4 — unchanged this window):

- **Delegate / quantization variants.** No INT8 / GPU / NNAPI delegate yet.
- **No net-new screens.** No `CaptureFailedScreen`, `Settings`, or `ModelInfoScreen`.
- **No ML training or retraining.**
- **`PlantIdentifier` / `IdentificationResult` interface changes.** Frozen per 0003 §4.4.
- **KB edits.** Locked.
- **AGP / Kotlin / Compose / Hilt / TFLite version bumps.** Locked.
- **README rewrite.** Append-only; link this roadmap instead.
- **`expected-artifacts` re-baselining.** Off the table.

Closed this window (PLANTPOTTING-0006 — removed, not carried):

- ~~**UX — subtitle leaks "model" jargon.**~~ Fixed: `R.string.low_conf_subtitle` now reads *"We're best at common houseplants — confirm or pick from the list below."*; contract test updated.
- ~~**UX — candidate chips render "(0%)" on degenerate captures.**~~ Fixed: floored-to-zero candidates render the name with no `(x%)` suffix and stay selectable; `LowConfidencePickerScreenTest` covers both the zero-suffix and normal-suffix cases.
- ~~**Calibration evidence rests on a single species.**~~ Closed: the `crassula-ovata` probe landed (@ 0.1055, low-conf), completing the multi-species sweep. `perSpeciesThresholds` is now probe-backed across both in-vocab species (and empty by design). The *result* of closing this gap is what elevated the model-swap milestone above.

Accepted as-is (recorded, **not** an open gap):

- **Failure-banner live visual (§7.6).** There is no production-accessible path (debug menu, long-press, BuildConfig-gated intent) to drive the camera into `CameraUiState.Failure` for a live visual check. The user explicitly waived this off — code review + JVM-test coverage (render + retry + state-clear) accepted as sufficient. Recorded for history only; it is **not** a carry-forward and should not be re-listed as a gap unless a future sprint makes Failure-state visual evidence load-bearing.

## Proposed Sprint Path

### Active horizon (detailed)

#### Next: PLANTPOTTING-0007 — Houseplant model-selection spike (V1 entry)
- **Intent:** V0.1 is **met** — calibration is mechanically complete and probe-backed across
  both in-vocab species. The sweep's *result* (jade @ 0.1055; 2 of 16 in-vocab) is the mandate:
  AIY Plants V1/3 is tuned for wild flora, so common houseplants route low-confidence by default.
  0007 opens **V1 — broad species coverage** with a model-selection spike: survey and evaluate
  candidate on-device classifiers with a houseplant-weighted vocabulary, against the AIY baseline.
- **Entry conditions:** PLANTPOTTING-0006 merged to `origin/main` (`status: done`). The probe
  harness (`OnDeviceModelRealInterpreterTest`), fixture + provenance pattern, and the frozen
  `PlantIdentifier` / `IdentificationResult` seam (0003 §4.4) are all reusable for evaluation.
- **Scope hints (to be firmed up in planning):**
  1. **Survey candidate models** — on-device, network-free, houseplant-weighted vocabulary
     (e.g. PlantNet-style or a fine-tuned classifier); compare licensing, size, and in-vocab
     coverage of the 16 KB species against the AIY V1/3 baseline.
  2. **Build a swap-evaluation harness** — re-probe the *same* Monstera + jade fixtures (plus a
     broader houseplant fixture set) against each candidate; compare top-1/top-3 and in-vocab
     coverage. Keep the evaluation behind the existing identifier seam.
  3. **Decide + re-baseline** — pick a model (or supplemental classifier); re-baseline
     `perSpeciesThresholds` / `_comment_coverage` against its vocabulary. Integration of the
     winning model may split into a follow-up sprint.
- **Milestone contribution:** opens **V1 — broad species coverage**; the exit bar is "most
  common houseplants identify directly, so the low-confidence path is the exception, not the rule."
- **Notes:**
  - This is the one milestone that requires touching the model itself — expect a multi-sprint arc
    (spike → integration → expanded validation set), not a single narrow sprint.
  - The frozen identifier interfaces (0003 §4.4) are the seam that makes the swap tractable —
    keep the change behind them.

### Milestone ladder (skeleton — sprints become detailed as they approach)

#### MVP — On-device ID + KB-driven potting-mix recommendation — *(shipped ~PLANTPOTTING-0003)*
Camera → on-device identification → routing decision → KB-driven recipe, network-free,
for the bundled species set.

#### V0.1 — Trustworthy confidence calibration — *(met ~PLANTPOTTING-0006)*
- **Exit criteria:** per-species thresholds backed by a real-photo probe ✓; an accuracy-bearing
  assertion in CI ✓; a multi-species probe sweep across both in-vocab species ✓.
- **Met:** real-photo probes for both in-vocab species (*Monstera deliciosa* @ 0.8984, high-conf;
  *Crassula ovata* @ 0.1055, low-conf) with accuracy assertions in `OnDeviceModelRealInterpreterTest`;
  the per-species threshold mechanism is wired and `perSpeciesThresholds` is empty by design
  (neither probe warranted an override). Further calibration depth requires a model swap (V1).

#### V1 — Broad species coverage *(active — next)*
- **Exit criteria:** most common houseplants identify directly (model swap, supplemental
  classifier, or expanded vocab) so the low-confidence path is the exception, not the rule.
- **Mandate:** V0.1's sweep proved the calibration plumbing is sound but the AIY V1/3 model is
  tuned for wild flora (jade @ 0.1055; 2 of 16 in-vocab) — the user's 0006 review feedback
  elevated the model swap from "later" to next-up.
- **Skeleton sprints:** model-selection spike (PLANTPOTTING-0007); replacement / supplemental
  classifier integration; expanded real-photo validation set.

#### V2 — Richer care guidance
- **Exit criteria:** beyond the one-shot recipe — repotting schedule, care reminders, or
  personalization to the user's plant set.
- **Skeleton sprints:** care-profile model; reminder UX; plant-collection persistence.

#### Beyond V2 — Plant-health diagnostics
- Identify stress / pest / over-watering signs from the same photo pipeline (sketch only).

## Changelog

- 2026-06-04 — bumped through PLANTPOTTING-0006 (review close-out): reconciled 0006 from
  in-progress → done. Completed the V0.1 multi-species calibration sweep — added the
  *Crassula ovata* probe (@ 0.1055, low-conf, honest-fallback assertion); `perSpeciesThresholds`
  stays empty by design. Closed the two 0005-review UX gaps (subtitle jargon, "(0%)" chips) and
  the single-species-evidence gap. Promoted calibration to `probe-backed (2 of 2 in-vocab)` and
  marked V0.1 **met**. **Elevated the model swap from a standing non-goal to the next-up V1
  milestone** — 0006's sweep + the user's review feedback show AIY V1/3 recognises wild flora,
  not houseplants. Re-scoped the active horizon to PLANTPOTTING-0007 (houseplant model-selection
  spike). Stats: ~8,455 LOC; 367/410 acceptance boxes (~90%).
- 2026-06-04 — refreshed through PLANTPOTTING-0005 (close-out): reconciled 0005 from
  in-progress → done. Closed the §5.4–§5.8 real-photo calibration chain, the §7.3
  integration-flow transcripts, and the §3.12/§4.7/§7.2 GMD-run gaps (removed from Known
  gaps, not carried). Opened 2 UX gaps (subtitle jargon, "(0%)" chips) + tightened the
  single-species-evidence gap; recorded the §7.6 Failure-banner visual as accepted-as-is.
  Advanced calibration from `emerging` → `probe-backed (1 of 2 in-vocab)`. Re-scoped
  PLANTPOTTING-0006 to the `crassula-ovata` probe + the two UX fixes (no longer "finish
  0005"); marked V0.1 partially met (✓ probe, ✓ CI assertion, ☐ multi-species sweep).
- 2026-06-04 — added roadmap-skill frontmatter (`last_updated`, `through_sid`), a
  Species/Model Coverage section, and reshaped "Candidate next sprint" into a Proposed
  Sprint Path (active horizon + milestone ladder). Content otherwise unchanged; no sprint
  window reconciled. First entry — the `/roadmap` skill appends one per REFRESH hereafter.
