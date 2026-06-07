# PLANTPOTTING-0012 Feedback

**Review date:** 2026-06-07
**Reviewer:** principal (whichrobevans@gmail.com)
**Verdict:** Sprint goals met, all gates green. **No code bugs.** One headline forward-looking
request (direct Pilea card) and one doc-cleanup item logged for the next sprint.

## Review gate results (all green)

- **JVM unit-test acceptance gates** — green: `ModelScoreMapperBoundaryGateTest`,
  `HousePlantClassMapValidationTest` (count 39, positive Pilea mapping, `pileaMappingRequiresBoundaryGate`
  bind-mapping-to-gate guard), `FixtureLicenseManifestTest`, `FixtureIntegrityTest`,
  `AccuracyEvalCsvSchemaTest`, `ModelManifestTest`.
- **Static gates** — `verifyNoNetworking` ✓, `ktlintCheck` ✓, `check-stub-isolation.sh` ✓.
- **Frozen seam** — `PlantIdentifier` / `IdentificationResult` / `IdSource` untouched in the exec diff ✓.
- **Evidence dir** — all required artifacts present (sourcing log, baseline eval+summary, boundary-gating
  decision, post-pilea gated eval+summary, tta-sweep csv+decision).
- **Artifacts** — v0.6.0 / versionCode 6 ✓; `plantpotting-v0.6.0-debug.apk` verified present in
  `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\` (2026-06-07 17:17).
- **Headline ML result (PASS):** adding Pilea behind the gate = **+0 confident-wrong** on every surface
  (clean / all perturbations / pothos-Pilea subset) vs **+2 / +9 / +6** for a naive gateless mapping.
  Correct-pothos picker-rate delta = **+0**. 0 pothos→direct-Pilea-card violations. All 6 true-Pilea
  fixtures route to picker with Pilea visible & first.

## Bugs

None. All acceptance criteria pass; the implementation did exactly what the plan specified.

## UX Issues

### Real, confident Pilea is demoted to the picker (strict-picker too conservative)
**What happened:** The principal tested the v0.6.0 build at home against a real Pilea peperomioides.
The app correctly surfaced **"Pilea (Chinese Money Plant) — 98%"** as the **top** candidate, with
"English Ivy 1%" and "ZZ plant" (no %) as the other two. But because the gate is scoped to
*top-1 = Pilea → always route to LowConfidencePicker* (strict-picker this sprint), Pilea never gets a
**direct care card** even when the model is confident and correct.
**Why it matters:** For a genuinely-Pilea photo the model is confident and right, yet the user still has
to tap through the picker to reach Pilea care info — extra friction on a *correct* identification. The
6 sourced Pilea fixtures earned the *right to evaluate* a direct card; this sprint deliberately shipped
strict-picker as the fail-safe, but the at-home result shows the model is reliable enough to warrant the
direct card.
**Key signal — the confusion is asymmetric:** On the real Pilea photo, **pothos did NOT appear as a
competing candidate** (the runners-up were English Ivy and ZZ plant). The dangerous boundary error is
one-directional — *pothos input → Pilea label* @ 0.9661 — not *Pilea input → pothos*. So a rule that
permits a direct Pilea card while still forcing the pothos→Pilea case to the picker is plausible.
**Suggested fix (next sprint):** Permit a **direct Pilea card** under an elevated evidence bar
(Candidate C — Pilea-specific `per_species_thresholds`, e.g. require Pilea top-1 well above the global
0.55 with a margin over second place), composed with the existing top-1=Pilea gate so the asymmetric
pothos→Pilea error still routes to the picker. Per the 0012 plan this requires **author-separated /
held-out evaluation** of the Pilea fixtures before a direct card is permitted. Watch overfit on the
thin (~6) Pilea set.

## Missing Features

- **Direct Pilea care card for confident, correct Pilea** — out of scope for 0012 (strict-picker was
  the agreed fail-safe), now the top candidate for the next sprint given the at-home confirmation.

## Documentation Issues

### Stale 0009-era Pilea-deferral prose in `docs/kb/ml-mapping-notes.md`
**What happened:** The new 0012 section (§"pothos↔Pilea boundary fix + Pilea now mapped") correctly
states the deferral is **LIFTED**, but earlier sections still contain the old 0009 prose
("Pilea deferral (deliberate, CI-enforced)… `pileaIsNotMapped` still passes… the pothos↔Pilea boundary
fix is still deferred", around lines ~270 and ~331). These now contradict reality.
**Why it matters:** A future planner/reader could be misled into thinking Pilea is still unmapped.
**Suggested fix:** In the next sprint, scrub or clearly mark the superseded 0009 deferral paragraphs as
historical (the per-sprint-section convention is fine, but the absolute-present-tense "still deferred"
claims should be corrected). ROADMAP.md is accurate and needs no change.

## Notes for Next Sprint

- The boundary gate is **deterministic and TTA-independent** (top-1=Pilea → picker), so TTA buys nothing
  for the boundary — confirmed by the sweep (tta stays 6; grid tiling dilutes the whole-image classifier
  and 3×3 busted the ~2s cap at 2686ms). Don't re-litigate TTA for this boundary.
- **8 previously-untested mapped species now have 1 CC0 fixture each** — coverage exists but each is a
  single base photo; deeper calibration on those remains unprobed.
- The optional public `v0.6.0` GitHub Release tag (plan line 147) was **not** pushed — no public release
  was requested. `versionName` bump alone never publishes; push the `v0.6.0` tag if/when a release is
  wanted.
- Real-world accuracy spot-check (Pilea) was a **clean correct hit** — a positive data point against the
  broader 0010 "confidently wrong" concern, at least for Pilea.
