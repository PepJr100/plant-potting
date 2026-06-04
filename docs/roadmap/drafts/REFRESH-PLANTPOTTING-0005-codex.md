## Species/Model Coverage

How much of the bundled KB the on-device model identifies verbatim, and the calibration state.

| Axis | State | Notes |
| --- | --- | --- |
| KB species | 16 | `species.json`; locked. |
| In-vocab vs AIY V1/3 | 2 of 16 | Only *Monstera deliciosa* and *Crassula ovata* overlap the AIY Plants V1/3 vocabulary verbatim (`_comment_coverage`). No model swap landed in 0005, so coverage count is unchanged. |
| Routing | threshold-gated | Top-1 score vs per-class threshold in `model_manifest.json` -> `ResultScreen` (high-conf) or `LowConfidencePicker` (low-conf / out-of-vocab). |
| Calibration | partial, evidenced | The calibration mechanism ships end-to-end: `ModelManifest.perSpeciesThresholds`, `ModelManifestReader` parsing, and `ModelScoreMapper` override-then-global threshold lookup. A real CC-BY-SA *Monstera deliciosa* probe on GMD identified `monstera-deliciosa` @ 0.8984, `lowConfidence=false`, `ON_DEVICE_MODEL`, and `OnDeviceModelRealInterpreterTest` now asserts the preferred accuracy claim (`speciesId == "monstera-deliciosa" && !lowConfidence`). `perSpeciesThresholds` intentionally ships empty because the probe cleared the 0.55 global threshold cleanly, not because evidence is missing. Full V0.1 calibration still needs a second in-vocab species sweep, starting with *Crassula ovata* in PLANTPOTTING-0006. |

## Known gaps

The PLANTPOTTING-0005 close-out removes the old "deferred from 0005 Blockers" carry-forward. The section 5.4-5.8 real-photo calibration chain is done: the CC-BY-SA Monstera fixture is bundled with provenance, the GMD probe recorded `monstera-deliciosa` @ 0.8984, the preferred accuracy-bearing assertion landed in `OnDeviceModelRealInterpreterTest`, and section 5.7 correctly left `perSpeciesThresholds` empty because no per-class override was warranted. Section 7.3 is also done: `integration-flow.ps1` cold, warm, and `-BuildOnly` transcripts are all GREEN. These are closed facts, not 0006 work.

Still-open product and platform gaps:

- **Model swap.** AIY V1/3 stays. Only 2 of 16 KB species are in-vocab verbatim; broader direct species coverage remains a later milestone.
- **Delegate / quantization variants.** No INT8 / GPU / NNAPI delegate yet.
- **No net-new screens.** No `CaptureFailedScreen`, `Settings`, or `ModelInfoScreen`.
- **No ML training or retraining.**
- **Calibration evidence is still single-species.** The Monstera probe firms up the mechanism and one in-vocab route, but V0.1's multi-species exit bar still requires probing the other in-vocab overlap, *Crassula ovata*. This is the calibration part of PLANTPOTTING-0006.
- **LowConfidencePicker subtitle copy leaks implementation jargon.** `R.string.low_conf_subtitle` currently says "model" / "vocabulary"; 0005 feedback recommends user-facing copy such as "We're best at common houseplants - confirm or pick from the list below."
- **LowConfidencePicker candidate chips can show `(0%)`.** Degenerate / black captures can produce tiny non-zero scores that floor to zero in the chip label. PLANTPOTTING-0006 should decide between hiding zero percentages, rendering `<1%`, or suppressing chips below a visibility threshold.
- **Failure-banner live visual accepted as-is.** The 0005 review could not exercise `CameraUiState.Failure` on a live emulator without source changes, but the user explicitly accepted code review plus JVM-test coverage as sufficient. Do not carry this as an open 0006 gap unless a future sprint makes Failure-state visual evidence load-bearing.
- **`PlantIdentifier` / `IdentificationResult` interface changes.** Frozen per 0003 section 4.4.
- **KB edits.** Locked.
- **AGP / Kotlin / Compose / Hilt / TFLite version bumps.** Locked.
- **README rewrite.** Append-only; link this roadmap instead.
- **`expected-artifacts` re-baselining.** Off the table.

## Proposed Sprint Path

### Active horizon (detailed)

#### Next: PLANTPOTTING-0006 - Crassula calibration probe + LowConfidencePicker UX fixes
- **Intent:** Extend calibration evidence from one in-vocab species to both AIY V1/3 <-> KB overlaps, and clean up the two user-facing `LowConfidencePicker` issues found in the 0005 review. This is no longer a "finish 0005" sprint; 0005 is closed.
- **Entry conditions:** PLANTPOTTING-0005 is merged and marked `done` in the ledger; the Monstera real-photo fixture, probe path, preferred accuracy assertion pattern, and empty-by-design `perSpeciesThresholds` decision already exist; `integration-flow.ps1` cold/warm/buildonly transcripts are green.
- **Scope hints:**
  - Source and bundle an auditable real-photo *Crassula ovata* fixture, following the Monstera fixture provenance pattern.
  - Run the same on-device GMD probe for *Crassula ovata* and record top-1, top-3, score, route, and source in sprint results and calibration notes.
  - Add an accuracy-bearing assertion for the Crassula fixture in the same spirit as the Monstera assertion. Prefer `speciesId == "crassula-ovata" && !lowConfidence` if the probe clears the global threshold; use an honest fallback only if the probe data requires it.
  - Seed `perSpeciesThresholds` only if the Crassula probe shows a per-class override is justified. If both in-vocab species clear the global threshold cleanly, keep the map empty by design and document that decision.
  - Rewrite `R.string.low_conf_subtitle` away from "model" / "vocabulary" jargon. Candidate wording: "We're best at common houseplants - confirm or pick from the list below."
  - Fix degenerate chip percentages by making an explicit UI policy decision: hide `(0%)`, show `<1%`, or suppress chips below a visibility threshold. Cover the chosen behavior with a focused test.
- **Milestone contribution:** advances **V0.1 - Trustworthy confidence calibration** from partially met toward exit by adding the required second in-vocab species probe, while also closing two polish findings from the 0005 review.
- **Out of scope:** finishing 0005 blockers, Failure-banner live visual work, model swap, KB expansion, interface changes, and expected-artifact re-baselining.

### Milestone ladder (skeleton - sprints become detailed as they approach)

#### MVP - On-device ID + KB-driven potting-mix recommendation - *(shipped ~PLANTPOTTING-0003)*
Camera -> on-device identification -> routing decision -> KB-driven recipe, network-free,
for the bundled species set.

#### V0.1 - Trustworthy confidence calibration *(partially met)*
- **Already met:** real-photo Monstera probe on GMD; preferred accuracy-bearing assertion in CI; per-species threshold mechanism wired; `perSpeciesThresholds` empty by design because Monstera cleared the global threshold cleanly.
- **Still pending:** multi-species fixture sweep across at least two in-vocab species, beginning with *Crassula ovata* in PLANTPOTTING-0006, plus any evidence-backed per-species threshold decision that falls out of that probe.
- **Skeleton sprints:** PLANTPOTTING-0006 (above); follow-on fixture/threshold sweeps TBD only if Crassula data exposes calibration work beyond the current mechanism.

#### V1 - Broad species coverage
- **Exit criteria:** most common houseplants identify directly (model swap, supplemental classifier, or expanded vocab) so the low-confidence path is the exception, not the rule.
- **Skeleton sprints:** model-selection spike; replacement/supplemental classifier integration; expanded real-photo validation set.

#### V2 - Richer care guidance
- **Exit criteria:** beyond the one-shot recipe - repotting schedule, care reminders, or personalization to the user's plant set.
- **Skeleton sprints:** care-profile model; reminder UX; plant collection persistence.

#### Beyond V2 - Plant-health diagnostics
Identify stress, pest, or over-watering signs from the same photo pipeline (sketch only).
