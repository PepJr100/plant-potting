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
the post-shutter UX is polished on both the high-confidence and the (more common)
low-confidence paths, and the `CameraUiState.Failure` state surfaces a persistent
bottom-anchored banner with a `Try again` retry. The test infrastructure has caught up
with PLANTPOTTING-0003's architectural debt — the global `TestIdentifyModule` is gone,
all seven dependent instrumentation tests use per-test `@BindValue`, and the previously
`@Ignore`'d `PermissionDeniedFlowTest` is reachable via `FakeCameraPermissionGuard`.
**Confidence calibration now has real evidence:** the `ModelManifest.perSpeciesThresholds`
mechanism (manifest + reader + `ModelScoreMapper` override-then-global) ships, and the
§5.4–§5.8 real-photo chain landed — a CC-BY-SA *Monstera deliciosa* photograph probes on
GMD as `monstera-deliciosa` @ **0.8984**, `lowConfidence=false`, clearing the 0.55 global
threshold cleanly. The §5.6 accuracy-bearing assertion guards it in CI (preferred form),
and `perSpeciesThresholds` ships **empty by design** — the probe showed no per-class
override was warranted. The §7.3 `integration-flow.ps1` cold + warm + buildonly transcripts
are all GREEN. PLANTPOTTING-0005 is closed.

_Derived stats: ~8,351 Kotlin LOC; 38 unit + 8 instrumentation test files; acceptance
checkboxes 334/384 ticked (~87%) across `docs/sprints/*.md` (the open boxes are the 0005
manual-GMD-walkthrough items the user accepted code + JVM coverage in lieu of)._

## Layer status

| Layer | Status | Notes |
| --- | --- | --- |
| KB (species.json, archetypes.json, plant_class_map.json) | ✓ shipped | 16 species, 18 mapping entries (2 overlap AIY V1 vocab). Locked. |
| Identifier (interface + on-device impl + stub) | ✓ shipped | `OnDevicePlantIdentifier` is production. Seam unchanged since 0003 §4.4. |
| Confidence calibration | △ partial | Mechanism shipped (`perSpeciesThresholds` in manifest, reader, mapper). Probe-backed for **1 of 2** in-vocab species (*Monstera deliciosa* @ 0.8984, high-conf); accuracy assertion in CI. Map empty by design — probe cleared the global cleanly. Multi-species sweep (*Crassula ovata*) pending in 0006. |
| Camera UI | ✓ shipped | Shutter, bind-pending overlay, Failure banner with `Try again`. |
| Result + Recommendation UI | ✓ shipped | Source-driven badge; archetype + recipe; sum-to-100 enforced. |
| LowConfidencePicker | ✓ shipped | Subtitle + chevron chips + empty-state cards + outlined archetype CTA + small-screen viewport assertion. Two copy/polish fixes queued for 0006 (see Known gaps). |
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
| Calibration | △ probe-backed (1 of 2 in-vocab) | **Firmed up this window.** One in-vocab species now has a real-photo, probe-backed high-confidence result (*Monstera deliciosa* @ 0.8984, `lowConfidence=false`), with an accuracy-bearing assertion guarding it in CI. `perSpeciesThresholds` is empty **by design** — the probe cleared the global threshold cleanly, so no override was warranted; the map is **not** empty for want of evidence. Advances past `emerging`. Full promotion (probe-backed across **both** in-vocab species) is 0006's multi-species sweep — *Crassula ovata* still to probe. |

## Sprint history

| SID | Title | Status | Headline outcome |
| --- | --- | --- | --- |
| PLANTPOTTING-0001 | Android scaffold + end-to-end stub flow with KB | done | Camera → result → recommendation flow on stub identifier; KB + Hilt + Compose scaffolding; integration-flow manifest baseline. |
| PLANTPOTTING-0002 | Fix sprint for PLANTPOTTING-0001 review bugs | done | Settings round-trip recovery (Bug 4); permission rationale wording; bind-pending overlay. |
| PLANTPOTTING-0003 | On-device ML identifier + Bug A fix + source-driven badge | done | `OnDevicePlantIdentifier` wired to AIY V1/3; source-driven `ResultScreen` badge; `LowConfidencePicker` v1; `_comment_coverage` (2 of 16 species in-vocab). |
| PLANTPOTTING-0004 | Fix sprint for PLANTPOTTING-0003 review bugs | done | UINT8 preprocessor branch (Bug 1); `testTagsAsResourceId` bridge (Bug 2); `OnDeviceModelRealInterpreterTest` lands. |
| PLANTPOTTING-0005 | Post-shutter polish + un-defer carry-forward from 0003/0004 | done | LowConfidencePicker polished; bottom-anchored Failure banner; `TestIdentifyModule` deleted (7 tests migrated to `@BindValue`); `LowConfidenceFlowTest` + un-`@Ignore`'d PermissionDenied; `perSpeciesThresholds` mechanism + real-photo probe (*Monstera deliciosa* @ 0.8984, high-conf; map empty by design); accuracy assertion in CI; integration-flow cold/warm/buildonly GREEN. |

## Known gaps

Standing non-goals (carried from 0005's §2.4 — unchanged this window):

- **Model swap.** AIY V1/3 stays. Only 2 of 16 KB species are in-vocab verbatim; broader direct species coverage is a later milestone.
- **Delegate / quantization variants.** No INT8 / GPU / NNAPI delegate yet.
- **No net-new screens.** No `CaptureFailedScreen`, `Settings`, or `ModelInfoScreen`.
- **No ML training or retraining.**
- **`PlantIdentifier` / `IdentificationResult` interface changes.** Frozen per 0003 §4.4.
- **KB edits.** Locked.
- **AGP / Kotlin / Compose / Hilt / TFLite version bumps.** Locked.
- **README rewrite.** Append-only; link this roadmap instead.
- **`expected-artifacts` re-baselining.** Off the table.

New gaps surfaced this window (from the 0005 review):

- **UX — subtitle leaks "model" jargon.** `R.string.low_conf_subtitle` reads *"This model recognises a limited plant vocabulary…"* — engineer-speak in a product surface. **Why it matters:** users don't think of the app as having "a model" with "a vocabulary"; the intent (set expectations about coverage limits) is right but the framing should be product-language. Fix queued for 0006 (suggested copy: *"We're best at common houseplants — confirm or pick from the list below."*). **Likely sprint:** PLANTPOTTING-0006.
- **UX — candidate chips render "(0%)" on degenerate captures.** On a black/degenerate preview the tiny non-zero scores round to zero, so chips read *"Jade plant (0%)"* — visually reads as broken. **Why it matters:** this is exactly what a poor-capture user sees. Fix queued for 0006 (decide hide-when-zero vs `<1%` vs suppress-below-visibility-threshold; cover with a `LowConfidencePickerScreenTest` case). **Likely sprint:** PLANTPOTTING-0006.
- **Calibration evidence rests on a single species.** The probe is real and clean, but it covers only `monstera-deliciosa`. *Crassula ovata* — the only other AIY V1/3 ↔ KB overlap per `_comment_coverage` — has not yet been probed, so `perSpeciesThresholds` is not yet backed across ≥2 species. **Why it matters:** V0.1's exit bar requires a multi-species sweep. This **replaces** the prior vague "single-species fixture" gap — the fixture exists and probed clean, so the remaining work is the *second* species. **Likely sprint:** PLANTPOTTING-0006.

Accepted as-is (recorded, **not** an open gap):

- **Failure-banner live visual (§7.6).** There is no production-accessible path (debug menu, long-press, BuildConfig-gated intent) to drive the camera into `CameraUiState.Failure` for a live visual check. The user explicitly waived this off — code review + JVM-test coverage (render + retry + state-clear) accepted as sufficient. Recorded for history only; it is **not** a carry-forward and should not be re-listed as a gap unless a future sprint makes Failure-state visual evidence load-bearing.

## Proposed Sprint Path

### Active horizon (detailed)

#### Next: PLANTPOTTING-0006 — Second in-vocab calibration probe (`crassula-ovata`) + two UX fixes
- **Intent:** 0005 is **done** — the Monstera real-photo probe, the CI accuracy assertion,
  and the integration transcripts all landed. So 0006 is **not** "finish 0005." It is
  narrower: extend the *existing* probe harness to the second and final in-vocab species,
  `crassula-ovata`, so `perSpeciesThresholds` is backed by real evidence across **both**
  AIY V1/3 ↔ KB overlaps (the multi-species sweep the V0.1 exit criteria require), and clear
  the two UX findings the 0005 review logged.
- **Entry conditions:** PLANTPOTTING-0005 merged to `origin/main` (`status: done` in the
  ledger). The reusable infrastructure already exists and is GREEN in CI — the Monstera
  fixture + provenance `LICENSE.txt` pattern, the `OnDeviceModelRealInterpreterTest` probe
  harness, and the `perSpeciesThresholds` mechanism (`model_manifest.json` +
  `ModelManifestReader` parse + `ModelScoreMapper` override-then-global). **No new infra
  needed** — 0006 reuses the proven 0005 path.
- **Scope hints:**
  1. **Source + bundle a CC-licensed `crassula-ovata` (jade plant) photo** — Wikimedia Commons
     CC-BY-SA / CC-BY / CC0, centre-cropped to match the Monstera fixture; append provenance
     to `identify-fixtures/LICENSE.txt` per the 0005 pattern.
  2. **Run the on-device GMD probe** on `pixel6Api34`; record top-1 / top-3 / score / route /
     source in the results doc and `ml-mapping-notes.md`, mirroring the Monstera entry.
  3. **Add the accuracy-bearing assertion for `crassula-ovata`** — preferred form
     (`speciesId == "crassula-ovata" && !lowConfidence`) if it clears the 0.55 global cleanly;
     honest fallback only if the probe data requires it. **Seed `perSpeciesThresholds` only if
     the probe shows a per-class override is justified** (the first real chance the map gains
     an entry). Record the seeding decision either way.
  4. **UX fix A — subtitle jargon.** Rewrite `R.string.low_conf_subtitle` to drop "model":
     *"We're best at common houseplants — confirm or pick from the list below."*
  5. **UX fix B — "(0%)" chips.** Fix the degenerate-capture rendering — decide between
     hide-`(x%)`-when-it-floors-at-zero, display `<1%`, or suppress chips below a visibility
     threshold; record the decision and cover it in `LowConfidencePickerScreenTest`.
- **Milestone contribution:** closes the multi-species-sweep gap for **V0.1 — Trustworthy
  confidence calibration**, exiting that milestone (with only 2 of 16 species in-vocab, the
  sweep is complete once 0006 lands).
- **Notes:**
  - Watch `FakeFixedIdentifier` knob growth (now 5 ctor params, 2 interfaces) — if 0006 adds a
    third knob or interface, split into focused fakes rather than accumulating (0005 review note).
  - Revisit `perSpeciesThresholds` top-1-only override semantics (margin path / lower-ranked
    mapped candidates) only if the `crassula-ovata` data forces real seeding (0005 review note).
  - Scope stays deliberately narrow — accuracy work is evidence-expensive and shouldn't sprawl.

### Milestone ladder (skeleton — sprints become detailed as they approach)

#### MVP — On-device ID + KB-driven potting-mix recommendation — *(shipped ~PLANTPOTTING-0003)*
Camera → on-device identification → routing decision → KB-driven recipe, network-free,
for the bundled species set.

#### V0.1 — Trustworthy confidence calibration *(partially met)*
- **Exit criteria:** per-species thresholds backed by a real-photo probe ✓; an accuracy-bearing
  assertion in CI ✓; a multi-species probe sweep across both in-vocab species ☐.
- **Met so far (PLANTPOTTING-0005):** real-photo probe (*Monstera deliciosa* @ 0.8984) + CI
  accuracy assertion in `OnDeviceModelRealInterpreterTest`; the per-species threshold mechanism
  is wired and `perSpeciesThresholds` is empty by design (Monstera cleared the global cleanly).
- **Pending (PLANTPOTTING-0006):** the `crassula-ovata` probe — the second and final in-vocab
  species. The sweep is complete once 0006 lands and V0.1 exits; further calibration depth would
  require a model swap (V1 territory).

#### V1 — Broad species coverage
- **Exit criteria:** most common houseplants identify directly (model swap, supplemental
  classifier, or expanded vocab) so the low-confidence path is the exception, not the rule.
- **Skeleton sprints:** model-selection spike; replacement / supplemental classifier
  integration; expanded real-photo validation set.

#### V2 — Richer care guidance
- **Exit criteria:** beyond the one-shot recipe — repotting schedule, care reminders, or
  personalization to the user's plant set.
- **Skeleton sprints:** care-profile model; reminder UX; plant-collection persistence.

#### Beyond V2 — Plant-health diagnostics
- Identify stress / pest / over-watering signs from the same photo pipeline (sketch only).

## Changelog

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
