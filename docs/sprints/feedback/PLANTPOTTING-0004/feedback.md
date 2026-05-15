# PLANTPOTTING-0004 Feedback

**Reviewed:** 2026-05-15 (same-day as execution).
**Reviewer:** user, in-session.
**Verdict:** clean review — no bugs, no UX issues, no missing features. Sprint goals
fully met. Both P0/P1 bugs from PLANTPOTTING-0003 are fixed; the §7.5 headline
transcripts that 0003 deferred are captured.

---

## Bugs

None. The PLANTPOTTING-0003 P0 (UINT8 dtype mismatch) and P1 (testTagsAsResourceId
bridge) both fixed on the live emulator. Reviewer walk-through reproduced the
captured transcripts: cold-boot `integration-flow.ps1` and `-BuildOnly` both
emitted `Integration manifest diff passed.`; the manual shutter→LowConfidencePicker
→ResultScreen→RecommendationScreen flow reached recipe rows with the expected
`source-badge=on-device match (low confidence)`.

The supplementary `tmp-dump.xml` captured during the walk-through (saved in this
feedback dir for the record) confirms `RecommendationScreen` exposes
`recommendation.archetypeName`, `recommendation.rationale`, `recommendation.recipeList`,
`recommendation.recipeRow` (×5: Pine bark 40%, Coco coir 25%, Perlite/pumice 20%,
Sphagnum 10%, Charcoal 5%), `recommendation.retake` — broadening §8.4's evidence
beyond the camera + result screens already in `evidence/`.

## UX Issues

None surfaced during review.

## Missing Features

None. Sprint scope was tightly narrowed (fix-only, matching the
PLANTPOTTING-0002→0001 pattern) and the implementer landed every must-land item
in §3.1 plus the contract-lock tests in §0.

## Notes for next sprint planner (PLANTPOTTING-0005)

These are observations from the review session, not bugs. The carry-forward list
in `results/PLANTPOTTING-0004.md` §5 already covers the deferred work
(`TestIdentifyModule` global removal + six-test `@BindValue` migration; confidence
calibration; `CameraUiState.Failure` UI polish; `LowConfidenceFlowTest`;
`PermissionDeniedFlowTest` un-`@Ignore`; INT8 / GPU / NNAPI). Items below add
texture from the review:

- **`LowConfidencePicker` is the headline post-shutter surface, not a fallback.**
  AIY V1/3 maps only 2 of 16 KB species verbatim per the manifest's
  `_comment_coverage`. The cold + warm transcripts plus the reviewer's
  walk-through all routed through `LowConfidencePicker`, even with a
  recognisably plant-shaped fixture. PLANTPOTTING-0005 UI polish (currently
  scoped at `CameraUiState.Failure`) should consider expanding to
  `LowConfidencePicker` polish — search-row visibility, "didn't find your plant"
  affordance, etc. The current screen is functional but feels skeletal next to
  `ResultScreen` and `RecommendationScreen`.

- **The Monstera fixture in `androidTest/assets/identify-fixtures/` is
  procedurally generated, not a real photo.** Reviewers / future planners
  reading `OnDeviceModelRealInterpreterTest` should not be surprised that the
  fixture deterministically routes to the low-conf path — the `_comment_coverage`
  expectation is that the model can't classify the synthetic radial-green
  pattern. The test's claim is only "doesn't throw + source == ON_DEVICE_MODEL"
  per Risk §7.6, and the falsifiability evidence at §4.6 proves it catches
  Bug-1-shaped regressions. If PLANTPOTTING-0005 introduces confidence-calibration
  testing, swap in a real CC-licensed Monstera photo at that point so accuracy
  assertions become meaningful.

- **No `docs/ROADMAP.md` exists yet in this project.** The `sprint-review` skill
  expects to update it at close-out. After four sprints, the ledger +
  results docs are doing the same job, but a single-page roadmap snapshot
  (current state, layer status, known gaps, next-sprint candidate) would be
  cheap to add the next time a planner is summarising state for an external
  reader. Not blocking — flagged as a docs-hygiene observation.

## Process notes (not bugs, environment-only)

- **`adb` not on the reviewer's PATH on this machine.** Manual `adb` invocations
  during the walk-through required the full
  `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe` path.
  `scripts/integration-flow.ps1` resolves this internally
  (`adb: C:\Users\robev\AppData\Local\Android\Sdk\platform-tools\adb.exe` in
  the transcript output), so script-level flows are unaffected. Future
  walk-through guides should call out the full-path form or suggest the user
  add platform-tools to `PATH` once.

- **Reviewer's `gradlew … bash scripts/check-stub-isolation.sh` chain failed
  with `Task 'bash' not found`.** PowerShell parsed the trailing `bash …` as
  a positional argument to `gradlew`. The two commands need to run separately
  (`gradlew …; bash scripts/check-stub-isolation.sh`) or via `&&`-equivalent
  chaining (`gradlew …; if ($?) { bash scripts/check-stub-isolation.sh }`).
  This is a sprint-review-skill walkthrough-drafting note, not a sprint
  regression — the gates themselves were verified during execution and the
  outputs are captured in `results/PLANTPOTTING-0004-phase1-verify.txt`.
