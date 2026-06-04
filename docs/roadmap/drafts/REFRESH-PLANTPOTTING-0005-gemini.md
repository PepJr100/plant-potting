## Known gaps

Carried forward from 0005's non-goals (§2.4):

- **Model swap.** AIY V1/3 stays. Only 2 of 16 KB species are in-vocab verbatim; calibration limits accepted.
- **Delegate / quantization variants.** No INT8 / GPU / NNAPI delegate yet.
- **No net-new screens.** No `CaptureFailedScreen`, `Settings`, or `ModelInfoScreen`.
- **No ML training or retraining.**
- **`PlantIdentifier` / `IdentificationResult` interface changes.** Frozen per 0003 §4.4.
- **KB edits.** Locked.
- **AGP / Kotlin / Compose / Hilt / TFLite version bumps.** Locked.
- **README rewrite.** Append-only; link this roadmap instead.
- **`expected-artifacts` re-baselining.** Off the table.

New gaps emerging post-0005:

- **`LowConfidencePicker` subtitle jargon.** The subtitle leaks the word "model" (`R.string.low_conf_subtitle`), reading as engineer-speak rather than product language. (Found in 0005 review).
- **Misleading "(0%)" chip text.** Candidate chips render "(0%)" on degenerate/black captures instead of hiding or showing "<1%". (Found in 0005 review).
- **Single-species calibration evidence.** The real-photo probe was highly successful for *Monstera deliciosa* (0.8984 score, clearing the global threshold cleanly), but calibration evidence still rests on this single species. A multi-species sweep is pending to fully back per-species thresholds.

*(Note: The `§5.4–§5.8` real-photo probe, accuracy assertion, and `§7.3` transcripts from 0005's blockers are all CLOSED and removed from this list. The `§7.6` Failure-banner live visual was waved off by the user as accepted-as-is via code review + JVM test coverage, so it is deliberately not tracked as a gap.)*

## Species/Model Coverage

How much of the bundled KB the on-device model identifies verbatim, and the calibration state.

| Axis | State | Notes |
| --- | --- | --- |
| KB species | 16 | `species.json`; locked. |
| In-vocab vs AIY V1/3 | 2 of 16 | Only *Monstera deliciosa* and *Crassula ovata* overlap the AIY Plants V1/3 vocabulary verbatim (`_comment_coverage`). |
| Routing | threshold-gated | Top-1 score vs per-class threshold in `model_manifest.json` → `ResultScreen` (high-conf) or `LowConfidencePicker` (low-conf / out-of-vocab). |
| Calibration | △ advancing | `perSpeciesThresholds` mechanism ships. One in-vocab species (*Monstera deliciosa*) now has a probe-backed high-confidence result. The map is empty **by design** (the probe cleared the global threshold cleanly), not by omission. Promotes to fully calibrated once the pending multi-species sweep backs thresholds across ≥2 species. |

## Proposed Sprint Path

### Active horizon (detailed)

#### Next: PLANTPOTTING-0006 — Crassula ovata probe + UX fixes
- **Intent:** Extend the calibration probe to the only other in-vocab species (*Crassula ovata*) to complete the multi-species sweep, and resolve the two UX polish findings from the 0005 review.
- **Entry conditions:** PLANTPOTTING-0005 is fully closed and merged. The *Monstera deliciosa* fixture, real-photo probe, and accuracy assertions are already in place and passing.
- **Scope hints:**
  - Source a *Crassula ovata* real-photo fixture, probe it, and seed `perSpeciesThresholds` only if the data demands an override.
  - Fix the `LowConfidencePicker` subtitle to remove the word "model" (e.g., "We're best at common houseplants — confirm or pick from the list below.").
  - Fix the candidate chips to not render "(0%)" on degenerate captures (decide between hide-when-zero, "<1%", or visibility threshold).
- **Milestone contribution:** Completes **V0.1 — Trustworthy confidence calibration**.
- **Note:** Scope is strictly narrowed to the multi-species sweep and remaining UX polish; 0005's deferred work is fully resolved.

### Milestone ladder (skeleton — sprints become detailed as they approach)

#### MVP — On-device ID + KB-driven potting-mix recommendation — *(shipped ~PLANTPOTTING-0003)*
Camera → on-device identification → routing decision → KB-driven recipe, network-free,
for the bundled species set.

#### V0.1 — Trustworthy confidence calibration *(partially met)*
- **Status:** Real-photo probe is done and an accuracy-bearing assertion is in CI (*Monstera deliciosa*).
- **Exit criteria:** per-species thresholds backed by a real-photo probe; an
  accuracy-bearing assertion in CI (met); a multi-species fixture sweep (pending *Crassula ovata*).
- **Skeleton sprints:** PLANTPOTTING-0006 (above) to complete the multi-species sweep.

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
