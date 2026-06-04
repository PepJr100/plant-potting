You are refreshing `docs/ROADMAP.md` for the PlantPotting Android project (on-device houseplant ID + KB-driven potting-mix recommendation, network-free).

Read these files first:
- `docs/ROADMAP.md` — the current roadmap.
- `docs/sprints/feedback/PLANTPOTTING-0005/feedback.md` — the 0005 review feedback.
- `docs/sprints/PLANTPOTTING-0005.md` — the sprint plan (read the `## Blockers` section at the very end especially).
- `docs/future-ideas/feature-ideas.md` — the idea inbox (it already reframes 0006).

WINDOW: PLANTPOTTING-0005 has now fully closed (status=done in the ledger). The current ROADMAP was written while 0005 was still in-progress with the §5.4–§5.8 calibration chain DEFERRED. KEY FACTS that landed this window and must be reconciled in:

- Real-photo calibration probe ran on the GMD: a real CC-BY-SA Monstera deliciosa photo (`File:HK_SW_Leaves_with_holes.JPG`, author *Princesleaf*) identifies as `monstera-deliciosa` @ 0.8984, lowConfidence=false, ON_DEVICE_MODEL — it clears the 0.55 global threshold CLEANLY.
- §5.6 accuracy-bearing assertion landed in `OnDeviceModelRealInterpreterTest` in the PREFERRED form: `speciesId == "monstera-deliciosa" && !lowConfidence`.
- §5.7 per-species seeding: NONE needed. `perSpeciesThresholds` ships EMPTY BY DESIGN (not by omission) — the probe showed no per-class override was warranted because the global threshold was cleared cleanly.
- §7.3 `integration-flow.ps1` cold + warm + buildonly transcripts all GREEN.
- The calibration MECHANISM (`perSpeciesThresholds` in the manifest + `ModelManifestReader` parse + `ModelScoreMapper` override-then-global) was already in place; it now has real probe-backed evidence for ONE species.
- The §7.6 Failure-banner LIVE visual was waved off by the user (code review + JVM-test coverage accepted as sufficient); it is NOT a carry-forward.

0006 RE-SCOPE (this is the key output): The idea inbox already reframes 0006. It is NO LONGER "finish 0005" — that work is done. It is narrower:
1. Extend the probe to the OTHER in-vocab species, `crassula-ovata` (the only other AIY V1/3 ↔ KB overlap per `_comment_coverage`), so `perSpeciesThresholds` gains real probe-backed evidence across ≥2 species — the multi-species sweep the V0.1 exit criteria require.
2. Fix the two 0005-review UX findings from the feedback:
   (a) `LowConfidencePicker` subtitle leaks "model" jargon — `R.string.low_conf_subtitle`; suggested rewrite "We're best at common houseplants — confirm or pick from the list below."
   (b) Candidate chips render "(0%)" on degenerate/black captures — decide hide-when-zero vs "<1%" vs suppress-chips-below-a-visibility-threshold.

Update ONLY these three sections (be concrete and well-sequenced):

- **Known Gaps reconciliation.** Which gaps the 0005 close-out CLOSED — the `§5.4–§5.8` real-photo probe + accuracy assertion + `§7.3` transcripts are all DONE now; those "deferred from 0005's Blockers" bullets in the current roadmap must be REMOVED, not carried. Which NEW gaps emerge — the 2 UX findings (subtitle jargon, "(0%)" chips); the fact that calibration evidence still rests on a SINGLE species (multi-species sweep pending); the waved-off Failure-banner live visual (note it as accepted-as-is, not an open gap). Justify each delta.
- **Species/Model Coverage.** Did calibration evidence firm up? Yes — one in-vocab species now has a probe-backed high-confidence result, so the calibration state advances from "emerging". But the map is empty BY DESIGN (probe cleared the global cleanly), NOT because evidence is missing — state this precisely. The multi-species sweep that would fully back per-species thresholds across ≥2 species is still 0006's job. In-vocab coverage is unchanged at 2 of 16 (no model swap).
- **Proposed Sprint Path.** Rebuild. Re-detail PLANTPOTTING-0006 with its NARROWED scope (crassula-ovata probe + the 2 UX fixes — NOT "finish 0005", which is done). Entry conditions should reflect that 0005 is merged and the Monstera fixture/probe already exist. Re-skeleton the milestone ladder. V0.1 "Trustworthy confidence calibration" is now PARTIALLY met (real-photo probe done, accuracy assertion in CI, multi-species sweep still pending) — reflect that precisely. Always sketch one milestone beyond V2.

Do NOT touch Current State, Layer Status, Sprint History, or the title/intent paragraph — the orchestrator handles those mechanically. Skeleton items beyond the active horizon stay one-liners.
