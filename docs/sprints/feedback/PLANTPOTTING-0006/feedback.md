# PLANTPOTTING-0006 Feedback

Review verdict: **clean execution, one major strategic direction surfaced.** All 38 task
boxes and all 10 acceptance criteria verified. No bugs. Both UX fixes approved. The review
did surface a milestone-level finding about the recognition engine itself (below).

## Bugs

None. All automated gates re-ran green during review:
- Focused JVM tests (`LowConfidencePickerScreenTest`, `LowConfidencePickerSubtitleContractTest`) — GREEN
- Full `testDebugUnitTest` + `ktlintCheck` — GREEN
- `check-stub-isolation.sh` — `stub isolation OK`
- `verifyNoNetworking` — GREEN
- GMD instrumented (`OnDeviceModelRealInterpreterTest`, 3 tests on `pixel6Api34`) — `BUILD SUCCESSFUL` (verified via captured evidence, not re-run)
- `integration-flow.ps1` cold/warm/buildonly — all "Integration manifest diff passed."

The committed crassula assertion (`realCrassulaPhotoRanksJadeTopMappedButRoutesLowConfidence`)
honestly matches the measured 0.1055 — low-confidence routing, jade ranks top among *mapped*
candidates, `source == ON_DEVICE_MODEL`. No silent strengthening of the assertion.

## UX Issues

None outstanding. Both 0005-logged UX fixes were reviewed and approved:
- **Subtitle de-jargon (G4):** "We're best at common houseplants — confirm or pick from the list below." — approved.
- **(0%) chip (G5):** floored-to-zero candidates render the name with no `(x%)` suffix and stay selectable; suffix retained for all ≥1%. — approved.

## Missing Features / Strategic Direction

### The recognition engine must be swapped for a houseplant-tuned model (HIGH PRIORITY)

**User feedback (verbatim intent):** "The recognition for houseplants needs to get much, much,
much, much better. The engine we have is recognising wildflowers. We need to swap it out for
something that recognises houseplants better soon."

**Why it matters / evidence:** The current model is **AIY Plants V1/3**, trained on a broad
wild-flora taxonomy. The 0006 multi-species sweep is itself the hard evidence:

| Species | In-vocab | Probe top-1 score | Route |
|---|---|---|---|
| `monstera-deliciosa` | yes | 0.8984 | high-confidence ✓ |
| `crassula-ovata` (jade) | yes | **0.1055** | **low-confidence** |

Jade — an extremely common houseplant — scores ~10% confidence even on a clean, canonical,
frame-filling fixture. And only **2 of 16** KB species are in the model vocabulary at all; the
other 14 route to `LowConfidencePicker` by design. The app's core promise (identify the plant
you're potting) is bottlenecked on a model that wasn't built for houseplants.

**Scope note:** Model swap was an *explicit non-goal* of 0005 and 0006 (those sprints
deliberately reused the proven harness to gather accuracy evidence first). That evidence is now
in hand and points one direction: the calibration mechanism (`perSpeciesThresholds`,
`ModelScoreMapper`, the probe harness) is sound, but the underlying model is the limiting factor.
The ROADMAP already lists "Model swap" as a later milestone — this feedback **elevates its
priority to next-up**.

**Fix direction (for the next sprint to scope, not decided here):**
- Survey candidate on-device models with a houseplant-weighted vocabulary (e.g. a PlantNet-style
  or fine-tuned classifier) that keeps the network-free / on-device constraints intact.
- Define a swap-evaluation harness: re-probe the *same* fixtures (Monstera + jade) plus a broader
  houseplant fixture set against the candidate model, comparing top-1/top-3 and in-vocab coverage
  against the AIY baseline.
- Preserve the frozen `PlantIdentifier` / `IdentificationResult` interfaces (0003 §4.4) so the
  swap is behind the existing seam.
- Re-baseline `perSpeciesThresholds` / `_comment_coverage` against the new model's vocabulary.

## Notes for Next Sprint

- **V0.1 (Trustworthy confidence calibration) is mechanically complete** — the multi-species
  sweep landed across both in-vocab species, and the calibration plumbing is proven. But the
  sweep's *result* is what motivates the model swap: calibration is only as useful as the model's
  ceiling, and the ceiling for houseplants is low.
- The ROADMAP was updated to post-0006 state as part of this review close-out (Calibration row
  promoted to "2 of 2 in-vocab, probe-backed"; the two UX gaps removed; a new "model recognises
  wild flora, not houseplants" known gap added pointing at the next milestone).
- Conditional cleanups (`perSpeciesThresholds` margin-semantics, `FakeFixedIdentifier` splitting)
  remain logged as "not forced" — revisit only if the model swap reopens them.
