<!--
DRAFT — REFRESH for the PLANTPOTTING-0005 close-out window.
Scope: ONLY the three sections below — Known gaps, Species/Model Coverage,
Proposed Sprint Path. Current state / Layer status / Sprint history / title +
intent paragraph are handled mechanically by the orchestrator and are NOT in
this draft. Splice these three sections in verbatim.
-->

## Known gaps

Standing non-goals (carried from 0005's §2.4 — unchanged this window):

- **Model swap.** AIY V1/3 stays. Only 2 of 16 KB species are in-vocab verbatim; calibration limits accepted.
- **Delegate / quantization variants.** No INT8 / GPU / NNAPI delegate yet.
- **No net-new screens.** No `CaptureFailedScreen`, `Settings`, or `ModelInfoScreen`.
- **No ML training or retraining.**
- **`PlantIdentifier` / `IdentificationResult` interface changes.** Frozen per 0003 §4.4.
- **KB edits.** Locked.
- **AGP / Kotlin / Compose / Hilt / TFLite version bumps.** Locked.
- **README rewrite.** Append-only; link this roadmap instead.
- **`expected-artifacts` re-baselining.** Off the table.

Closed this window (PLANTPOTTING-0005 close-out — **removed**, not carried). These
were the "deferred from 0005's Blockers" bullets in the prior roadmap; the close-out
session landed all of them, so they are no longer gaps:

- **§5.4–§5.8 real-photo calibration chain — DONE.** A real CC-BY-SA Monstera
  deliciosa photo (`File:HK_SW_Leaves_with_holes.JPG`, author *Princesleaf*, centre-cropped
  480×480) probes as `monstera-deliciosa` @ **0.8984**, `lowConfidence=false`,
  `ON_DEVICE_MODEL` — clearing the 0.55 global threshold cleanly. The accuracy-bearing
  assertion landed in `OnDeviceModelRealInterpreterTest` in the **preferred** form
  (`speciesId == "monstera-deliciosa" && !lowConfidence`), and `perSpeciesThresholds`
  ships **empty by design** — §5.7 seeding was evaluated and correctly declined because no
  per-class override was warranted. *Justification: these were the four open Blockers; each
  is now evidenced in the 0005 results doc and CI, so carrying them as gaps would misstate
  current state.*
- **§7.3 `integration-flow.ps1` cold + warm + buildonly transcripts — DONE.** All three
  GREEN (`Integration manifest diff passed.`) on a booted `Pixel_6_API_34` emulator. *Justification:
  the manifest-drift risk is closed with captured transcripts; no longer pending.*
- **§3.12 / §4.7 / §7.2 GMD instrumentation runs — DONE.** Exercised green by the §5.8
  real-model run and by CI. *Justification: the GMD path is proven, not deferred.*

New gaps surfaced this window:

- **UX — subtitle leaks "model" jargon.** `R.string.low_conf_subtitle` reads *"This model
  recognises a limited plant vocabulary…"* — engineer-speak in a product surface. Fix queued
  for 0006 (suggested copy: *"We're best at common houseplants — confirm or pick from the
  list below."*). *Justification: a concrete, reviewer-logged finding from the 0005 emulator
  walkthrough; not present in the prior roadmap.*
- **UX — candidate chips render "(0%)" on degenerate captures.** On a black/degenerate
  preview the tiny non-zero scores round to zero, so chips read *"Jade plant (0%)"* — visually
  broken. Fix queued for 0006 (decide hide-when-zero vs `<1%` vs suppress-below-visibility-
  threshold). *Justification: reviewer-logged, reproducible on `Pixel_6_API_34`; new this window.*
- **Calibration evidence rests on a single species.** The probe is real and clean, but it
  covers only `monstera-deliciosa`. `crassula-ovata` — the only other AIY V1/3 ↔ KB overlap
  per `_comment_coverage` — has not yet been probed, so `perSpeciesThresholds` is not yet
  backed across ≥2 species. This **replaces** the prior "single-species fixture" bullet, which
  is now imprecise: the fixture exists and probed clean, so the gap is the *second* species,
  not the first. The multi-species sweep is 0006's job. *Justification: tightens a vague
  standing gap into the specific remaining work.*

Accepted as-is (recorded, **not** an open gap):

- **Failure-banner live visual (§7.6).** There is no production-accessible path (debug menu,
  long-press, BuildConfig-gated intent) to drive the camera into `CameraUiState.Failure` for
  a live visual check. The user explicitly waived this off — code review + JVM-test coverage
  (render + retry + state-clear) accepted as sufficient. Recorded here for history only; it is
  **not** a carry-forward and should not be re-listed as a gap. *Justification: the close-out
  note marks it accepted-as-is; logging it as open would re-open a closed decision.*

## Species/Model Coverage

How much of the bundled KB the on-device model identifies verbatim, and the calibration state.

| Axis | State | Notes |
| --- | --- | --- |
| KB species | 16 | `species.json`; locked. |
| In-vocab vs AIY V1/3 | 2 of 16 | Unchanged — no model swap. Only *Monstera deliciosa* and *Crassula ovata* overlap the AIY Plants V1/3 vocabulary verbatim (`_comment_coverage`). |
| Routing | threshold-gated | Top-1 score vs per-class threshold (`perSpeciesThresholds`) falling back to the 0.55 global in `model_manifest.json` → `ResultScreen` (high-conf) or `LowConfidencePicker` (low-conf / out-of-vocab). |
| Calibration | △ probe-backed (1 of 2 in-vocab) | **Firmed up this window.** One in-vocab species now has a real-photo, probe-backed high-confidence result (*Monstera deliciosa* @ 0.8984, `lowConfidence=false`), and an accuracy-bearing assertion guards it in CI. `perSpeciesThresholds` is empty **by design** — the probe cleared the global threshold cleanly, so no override was warranted; the map is NOT empty for want of evidence. Advances past `emerging`. Full promotion (probe-backed across **both** in-vocab species) is 0006's multi-species sweep — `crassula-ovata` still to probe. |

Calibration evidence firmed up: the prior roadmap's `emerging` state described a mechanism
with an empty map and *no* probe data. That has changed — the mechanism now carries real,
single-species probe evidence. The precise distinction worth holding: the map is empty because
the one probed species *cleared the global cleanly* (a positive result), not because evidence
is missing. In-vocab coverage itself is unchanged at 2 of 16 — no model was swapped, so the
ceiling on direct identification is the same; only the *confidence* of routing those two species
improved.

## Proposed Sprint Path

### Active horizon (detailed)

#### Next: PLANTPOTTING-0006 — Second in-vocab calibration probe (`crassula-ovata`) + two UX fixes
- **Intent:** 0005 is **done** — the Monstera real-photo probe, the CI accuracy assertion, and
  the integration transcripts all landed. So 0006 is **not** "finish 0005." It is narrower:
  extend the *existing* probe harness to the second and final in-vocab species, `crassula-ovata`,
  so `perSpeciesThresholds` is backed by real evidence across **both** AIY V1/3 ↔ KB overlaps
  (the multi-species sweep the V0.1 exit criteria require), and clear the two UX findings the
  0005 review logged.
- **Entry conditions:** 0005 merged to `origin/main` (`status: done` in the ledger). The
  reusable infrastructure already exists and is GREEN in CI — the Monstera fixture + provenance
  `LICENSE.txt` pattern, the `OnDeviceModelRealInterpreterTest` probe harness, and the
  `perSpeciesThresholds` mechanism (`model_manifest.json` + `ModelManifestReader` parse +
  `ModelScoreMapper` override-then-global). **No new infra needed** — 0006 reuses the proven
  0005 path.
- **Scope:**
  1. **Source + bundle a CC-licensed `crassula-ovata` (jade plant) photo** — Wikimedia Commons
     CC-BY-SA / CC-BY / CC0, centre-cropped to match the Monstera fixture; append provenance
     to `identify-fixtures/LICENSE.txt` per the 0005 pattern.
  2. **Run the on-device probe** on `pixel6Api34`; record top-1 / top-3 / score / route in the
     results doc, mirroring the Monstera entry.
  3. **Add the accuracy-bearing assertion for `crassula-ovata`** — preferred form
     (`speciesId == "crassula-ovata" && !lowConfidence`) if it clears the 0.55 global cleanly;
     **seed `perSpeciesThresholds` only if the probe data demands it** (the first real chance the
     map gains an entry). Record the seeding decision either way.
  4. **UX fix A — subtitle jargon.** Rewrite `R.string.low_conf_subtitle` to drop "model":
     *"We're best at common houseplants — confirm or pick from the list below."*
  5. **UX fix B — "(0%)" chips.** Fix the degenerate-capture rendering — decide between
     hide-the-`(x%)`-when-it-floors-at-zero, display `<1%`, or suppress chips below a visibility
     threshold; record the decision and cover it in `LowConfidencePickerScreenTest`.
- **Milestone contribution:** closes the multi-species-sweep gap for **V0.1 — Trustworthy
  confidence calibration**, exiting that milestone.
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
- **Met so far (PLANTPOTTING-0005):** real-photo probe (`monstera-deliciosa` @ 0.8984) +
  CI accuracy assertion in `OnDeviceModelRealInterpreterTest`.
- **Pending (PLANTPOTTING-0006):** the `crassula-ovata` probe — the second and final in-vocab
  species. With only 2 of 16 species in-vocab, the sweep is complete once 0006 lands, and V0.1
  exits; further calibration depth would require a model swap (V1 territory).

#### V1 — Broad species coverage
- **Exit criteria:** most common houseplants identify directly (model swap, supplemental
  classifier, or expanded vocab) so the low-confidence path is the exception, not the rule.
- **Skeleton sprints:** one-liners (rough) — concretised when V0.1 exits.

#### V2 — Richer care guidance
- **Exit criteria:** beyond the one-shot recipe — repotting schedule, care reminders, or
  personalization to the user's plant set.
- **Skeleton sprints:** one-liners (rough).

#### Beyond V2 — Plant-health diagnostics
- Identify stress/pest/over-watering signs from the same photo pipeline (sketch only).
