# PLANTPOTTING-0005 Feedback

Review conducted 2026-05-16 against `137f216` (sprint head, before this feedback
commit). Sprint stays `in-progress`; §5.4–§5.8 + §7.2 + §7.3 + most of §7.6 remain
the documented carry-forward into PLANTPOTTING-0006. The reviewer drove a live
emulator walkthrough of `LowConfidencePicker` (4 screencaps in `evidence/`) and
attempted but couldn't reproduce the `CameraUiState.Failure` state from outside
the app — see UX Issues below.

## Bugs

(None this review.)

## UX Issues

### `LowConfidencePicker` subtitle copy leaks model jargon

**What happened:** The subtitle row added in §1.1 reads *"This model recognises a
limited plant vocabulary — please confirm or pick below."* The word "model" is
internal terminology that doesn't belong in user-facing copy.

**Why it matters:** Users don't think of the app as having "a model" with "a
vocabulary." It reads as engineer-speak in a product surface. The intent — set
expectations that on-device identification has limits — is right; the framing
should be product-language.

**Suggested fix (PLANTPOTTING-0006):** Replace with something like *"We're best
at common houseplants — confirm or pick from the list below."* Plan §1.1 explicitly
allows the executor to revise copy — recording the revision here so 0006 picks it
up. `R.string.low_conf_subtitle`.

### `LowConfidencePicker` chips show "(0%)" on degenerate captures

**What happened:** On a black emulator preview (no host camera), the on-device
model produces scores low enough that the chip-label `probabilityPct` rounds to
zero. The screen renders *"Jade plant (0%)"* and *"Swiss cheese plant (0%)"* —
visually reads as broken.

**Why it matters:** This is the exact scenario a user with a poor-quality capture
would see. "(0%)" is misleading; the underlying scores are non-zero but tiny.

**Suggested fix (PLANTPOTTING-0006):** Either (a) hide the `(x%)` text when the
score floors at zero, (b) display `<1%` instead, or (c) suppress chips entirely
when the score is below some visibility threshold. Decision belongs to whoever
plans the copy revision above.

**Provenance:** observed on emulator `Pixel_6_API_34` capture saved at
`docs/sprints/evidence/PLANTPOTTING-0005/02-after-shutter.png`.

## Missing Features

### `CameraUiState.Failure` cannot be exercised on a live device without source changes

**What:** There is no production-accessible path (debug menu, long-press,
BuildConfig-gated intent) to drive the camera into `Failure` state for visual
verification. The reviewer tried (a) revoking the runtime CAMERA permission
mid-session (kills the process) and (b) `appops set CAMERA deny` (bypassed by the
already-bound `ImageCapture` session). Neither produced a Failure banner.

**Why it matters:** §7.6 of the plan asks for a manual emulator walkthrough that
includes the polished Failure banner. JVM tests cover render + retry + state-clear,
but the live visual remains unverified. The plan note says *"manually trigger a
capture failure (e.g. block ImageCapture momentarily or use a forced-failure debug
toggle if one exists; otherwise simulate by injecting Failure state via a debug-only
path)"* — that toggle was never added.

**Status:** The user explicitly waved this off as "no carry-forward needed" — code
review + JVM test coverage accepted as sufficient evidence. Recording here so a
future planner has the history if a real Failure-state visual ever becomes load-
bearing.

## Notes for Next Sprint

### Watch: `FakeFixedIdentifier` knob growth

`FakeFixedIdentifier` now carries 5 constructor params (`speciesId`, `displayName`,
`source`, `lowConfidence`, `seedCandidates`) and implements two interfaces
(`PlantIdentifier`, `CandidateProvider`). Still small (~30 LOC, all defaults), but
post-`TestIdentifyModule` removal it's the only shared test seam for identification.
If 0006 (or later) adds more knobs or a third interface, split into focused fakes
(`FakeLowConfidenceIdentifier`, `FakeUnmappedIdentifier`) rather than letting one
class accumulate every test's needs.

### Discussion: `perSpeciesThresholds` top-1-only override semantics

`ModelScoreMapper.kt:55-66` consults `perSpeciesThresholds[bestSpeciesId]` for the
top-1 candidate only — matches existing `Thresholds.highConfidencePlain` semantics
(also top-1 vs `bestProb`). When 0006 starts seeding overrides for real, consider
whether the override should also apply to:
- The margin path (`_margin_min` / `_margin_delta`) — currently deferred per §4.3
  (premature surface).
- Lower-ranked mapped candidates when top-1 is unmapped.

Not a blocker for shipping the mechanism — log as a calibration-design discussion
item that the data may force a decision on.

### Partial §7.6 emulator walkthrough completed in this review

Saved to `docs/sprints/evidence/PLANTPOTTING-0005/`:

- `01-camera.png` — Camera screen with shutter visible (black preview because
  emulator has no host camera).
- `02-after-shutter.png` — `LowConfidencePicker` rendered post-capture. Confirms
  subtitle, chevron chips, outlined CTA, bottom-anchored OutlinedButton land
  correctly. Also exposes the `(0%)` UX issue above.
- `03-search-empty.png` — `SEARCH_EMPTY` state with copy *"No species match
  \"zzzz\""*. Confirms the `Box(weight=1f)` layout keeps the species-list region
  consistent and the empty-state row replaces the `LazyColumn` cleanly.
- `04-failure-attempt1.png` / `05-failure-attempt3.png` — failed attempts to force
  `Failure` state (see Missing Features above).

The bonus accidental ResultScreen render (`Monstera deliciosa` low-confidence
badge) is not saved as its own file but was visible in the screenshot returned
during the search-field tap detour — confirms the source-badge invariant
*"On-device match (low confidence)"* still holds for user-picked candidates.

`NO_CANDIDATES_EMPTY` could not be exercised from a live emulator (the production
model always emits some mapped candidates against the AIY V1 vocabulary) — JVM
test coverage in `LowConfidencePickerScreenTest` is sufficient.

### Items still deferred to PLANTPOTTING-0006

Per the plan's `## Blockers` section and ROADMAP "Known gaps":

- §5.4 — source + bundle a CC-licensed Monstera deliciosa photograph.
- §5.5 — run the on-device probe on GMD.
- §5.6 — pick the accuracy-bearing assertion (preferred vs fallback per probe).
- §5.7 — conditionally seed `perSpeciesThresholds` for at most one species.
- §3.12 / §4.7 / §7.2 — GMD instrumentation runs.
- §7.3 — `integration-flow.ps1` cold + warm + buildonly transcripts from pwsh.
- §7.6 — Failure-banner live visual (the LowConfidencePicker piece is done above).
