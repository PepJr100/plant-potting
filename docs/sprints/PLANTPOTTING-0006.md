# PLANTPOTTING-0006 — Second in-vocab calibration probe (`crassula-ovata`) + two UX fixes

Extend the proven PLANTPOTTING-0005 probe harness to the **second and final in-vocab species,
`crassula-ovata` (jade plant)**, so `perSpeciesThresholds` is backed by real-photo evidence
across *both* AIY V1/3 ↔ KB overlaps — completing the V0.1 multi-species sweep and exiting that
milestone — and clear the two UX findings the 0005 review logged (subtitle jargon, "(0%)" chips).
Deliberately narrow: accuracy evidence is expensive, so we **reuse every 0005 mechanism and add
zero infrastructure**.

## Goals

- **G1 — Real-photo evidence for `crassula-ovata`.** Source/bundle a CC-licensed jade-plant fixture,
  run the on-device GMD probe, and record top-1 / top-3 / score / route / source, mirroring the
  Monstera entry. This is the last AIY V1/3 ↔ KB overlap; landing it completes the V0.1 sweep.
- **G2 — CI-guarded accuracy assertion for `crassula-ovata`** in `OnDeviceModelRealInterpreterTest`,
  preferring the strong form (`speciesId == "crassula-ovata" && !lowConfidence`), with an honest,
  documented fallback only if the probe data demands it.
- **G3 — Evidence-driven `perSpeciesThresholds` decision.** Seed the map *only* if the probe shows a
  per-class override is justified; record the decision (and the deciding number) either way. This is
  the first real chance the map gains an entry — so prove it doesn't perturb Monstera.
- **G4 — UX fix A:** de-jargon `R.string.low_conf_subtitle` (drop "model").
- **G5 — UX fix B:** fix the degenerate `(0%)` chip rendering by **dropping the `(x%)` suffix when it
  floors to zero**, covered in `LowConfidencePickerScreenTest`.

## Non-goals (do not plan)

Model swap / new model; delegate or quantization variants (INT8 / GPU / NNAPI); net-new screens
(`CaptureFailedScreen`, `Settings`, `ModelInfoScreen`); ML training or retraining; changes to the
`PlantIdentifier` / `IdentificationResult` interfaces (frozen, 0003 §4.4); KB edits to
`species.json` / `archetypes.json` / `plant_class_map.json`; AGP / Kotlin / Compose / Hilt / TFLite
version bumps; README rewrite (append-only); `expected-artifacts` re-baselining; any net-new effort
on the `CameraUiState.Failure` live visual (user-waived — JVM coverage accepted).

## Scope boundaries

- **Reuse, don't build.** Fixture + provenance pattern
  (`app/src/androidTest/assets/identify-fixtures/` + `LICENSE.txt`), the
  `OnDeviceModelRealInterpreterTest` probe harness, and the `perSpeciesThresholds` mechanism
  (`model_manifest.json` `per_species_thresholds` → parse in
  `identify/model/ModelManifest.kt` → override-then-global in `identify/model/ModelScoreMapper.kt`,
  top-1 vs `bestProb`). No new test classes for the probe, no new assets dirs, no new manifest fields.
- **One photo, one species.** No speculative probing of out-of-vocab species; the other 14 KB species
  are not in the model vocabulary and route to `LowConfidencePicker` by design.
- **The two adjacent-cleanup items are *conditional*, not planned work.** Each is gated on the
  crassula data actually forcing it (see Phase 3 + Phase 5). If the trigger does not fire, the
  deliverable is a one-paragraph "logged, not built" note in `docs/kb/ml-mapping-notes.md` — nothing
  more. This guard is load-bearing for scope discipline.

## Task list

### Phase 1 — Fixture & provenance

- [ ] Select a `crassula-ovata` (jade plant) photo on Wikimedia Commons under CC-BY-SA / CC-BY / CC0,
      depicting an **unambiguous** jade plant (fleshy oval leaves, woody stem; avoid flowering
      close-ups or mixed-succulent arrangements that could pull top-1 to a neighbouring class).
- [ ] Centre-crop and scale to **480×480**, re-encode **JPEG quality 80**, to match the Monstera
      fixture's dimensions/encoding exactly; save as
      `app/src/androidTest/assets/identify-fixtures/crassula-ovata.jpg`. (Confirm the Monstera
      fixture's actual dims/encoding first and mirror them if they differ from 480×480/q80.)
- [ ] Append a `crassula-ovata.jpg` provenance block to
      `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`, following the 0005 Monstera block
      **verbatim in structure**: Title / Depicts / Source page / Original file / Author / Date taken /
      Retrieved / Modifications / License + full attribution string + test-only/not-in-APK note.
- [ ] Confirm the asset lives under `app/src/androidTest/` **only** (test-only; not bundled into the
      production APK) and that the merged-androidTest-assets copy picks it up on the next build.
- [ ] Verify the fixture is readable at runtime from androidTest assets via the **same code path** as
      `monstera-deliciosa.jpg` (load it in the probe test before asserting on it), so a bad
      crop/encoding fails loudly rather than silently mis-probing.

### Phase 2 — Probe, record, assert

- [ ] Add a crassula probe path to `OnDeviceModelRealInterpreterTest` that loads
      `identify-fixtures/crassula-ovata.jpg`, runs the real `OnDevicePlantIdentifier.identify(...)`,
      and surfaces top-1 id, top-1 score, the top-3 candidates, `lowConfidence`, and `source`.
- [ ] Run the test on the `pixel6Api34` Gradle Managed Device and capture the raw probe numbers
      (top-1 / top-3 / score / route / source) into the sprint evidence trail
      (`docs/sprints/evidence/PLANTPOTTING-0006/`).
- [ ] Record the crassula probe outcome in `docs/kb/ml-mapping-notes.md` under the existing
      `Crassula ovata → crassula-ovata` section, mirroring the Monstera "Probe outcome" paragraph
      (measured top-1 id + p, lowConfidence, source, other mapped candidates, route taken).
- [ ] Record the same outcome in the sprint results doc `docs/sprints/results/PLANTPOTTING-0006.md`,
      co-located with the 0005 Monstera result, so the multi-species sweep evidence sits together.
- [ ] Commit the accuracy-bearing assertion in `OnDeviceModelRealInterpreterTest`: **preferred form**
      `speciesId == "crassula-ovata" && lowConfidence == false && source == ON_DEVICE_MODEL` if the
      probe clears the 0.55 global cleanly. **Only if** the probe data requires it, commit the
      documented honest fallback instead and record *why* in the test comment + mapping notes — no
      silent weakening. **Do not pre-commit the assertion form before the probe number is in hand.**

### Phase 3 — `perSpeciesThresholds` decision (evidence-gated)

- [ ] Decide whether `crassula-ovata` warrants a `per_species_thresholds` entry: justified **only** if
      top-1 is the correct class but its score falls below the 0.55 global by a margin an absolute
      per-class override would cleanly close (per `model_manifest.json`
      `_comment_per_species_thresholds`). If it clears the global, the map stays empty by design.
- [ ] If justified: seed `per_species_thresholds["crassula-ovata"] = <value>` in
      `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json`; confirm `ModelManifest.kt` parses it
      and `ModelScoreMapper.kt` applies override-then-global. Update the threshold contract tests
      (`PerSpeciesThresholdsContractTest`, `ModelScoreMapperPerSpeciesThresholdTest`) to reflect the
      now-populated map. Otherwise leave the map empty.
- [ ] **Cross-class regression guard (neither draft had this — load-bearing).** Immediately after any
      seeding, **re-run the Monstera assertion** in `OnDeviceModelRealInterpreterTest` and confirm
      `monstera-deliciosa` still probes @ ~0.8984, `lowConfidence=false` via the global path. This map's
      first-ever populated state is the moment to prove `ModelScoreMapper`'s override-then-global doesn't
      perturb the other class. If the map stays empty, this is a no-op (note it).
- [ ] Record the seeding decision (seeded-with-value **or** not-warranted, with the deciding number) in
      `docs/kb/ml-mapping-notes.md` regardless of outcome.
- [ ] **Conditional — only if real crassula seeding forces it:** revisit `perSpeciesThresholds`
      top-1-only override semantics (margin path `_margin_min` / `_margin_delta`, and lower-ranked
      mapped candidates when top-1 is unmapped). If the data does not force a decision, **log the open
      question in the mapping notes and do not build it.**

### Phase 4 — UX fix A (subtitle jargon)

- [x] Rewrite `R.string.low_conf_subtitle` in `app/src/main/res/values/strings.xml` to drop "model".
      Use: **"We're best at common houseplants — confirm or pick from the list below."**
- [x] Update `LowConfidencePickerSubtitleContractTest` (and any other usage of `low_conf_subtitle`)
      so the suite asserts the new copy — the rewrite turns this contract test red otherwise.

### Phase 5 — UX fix B (`(0%)` chips)

- [x] Implement the merge-decided behaviour: when a candidate's `probabilityPct` floors to `0`, render
      the chip as the **name with no `(x%)` suffix** (e.g. "Jade plant"), not "Jade plant (0%)". Change
      is localized to the chip-text builder in `LowConfidencePickerScreen.kt`; keep the suffix for all
      `>= 1%` candidates unchanged. (Rationale recorded in §"UX fix B decision" below.)
- [x] Keep the candidate **selectable** — the fix is presentational only. A zero-score candidate must
      remain present and pickable; this is a *picker*, so dropping/disabling the chip would be a
      functional regression.
- [x] Extend `LowConfidencePickerScreenTest` to cover: (a) a `0%` candidate renders the name with **no**
      `(…%)` suffix and is still present/clickable, and (b) a normal candidate (e.g. `(72%)`) still
      renders its suffix. Use the existing `testTagsAsResourceId` bridge for node matching.
- [x] **Conditional — only if Phase 5 would add a third knob/interface to `FakeFixedIdentifier`:** split
      it into focused fakes (`FakeLowConfidenceIdentifier`, `FakeUnmappedIdentifier`) rather than
      accumulating (it is already at 5 ctor params + 2 interfaces). If no new knob is needed, leave it
      and note "not forced" in the mapping notes. → **Not forced** — the chip fix added no fake knob;
      logged in `docs/kb/ml-mapping-notes.md`.

### Phase 6 — Close-out gates (cheap-failure-first ordering)

- [ ] **Focused JVM tests first** (so failures are cheap to diagnose): `LowConfidencePickerScreenTest`,
      `LowConfidencePickerSubtitleContractTest`, and — only if a threshold was seeded —
      `ModelScoreMapperPerSpeciesThresholdTest` / `PerSpeciesThresholdsContractTest`; all green.
- [ ] Run the instrumented `OnDeviceModelRealInterpreterTest` (Monstera **and** crassula) on
      `pixel6Api34`; green.
- [ ] Run the full JVM unit suite; green.
- [ ] Confirm the stub-isolation gate (`scripts/check-stub-isolation.sh`) remains GREEN.
- [ ] Confirm the network-free gate (`verifyNoNetworking`) remains GREEN.
- [ ] Run `scripts/integration-flow.ps1` (cold / warm / buildonly) and confirm GREEN.
- [ ] Declare the V0.1 multi-species sweep complete in `docs/sprints/results/PLANTPOTTING-0006.md`
      and flip the sprint ledger entry for PLANTPOTTING-0006 (only after all gates above are green).

## Sequencing & dependencies

```
Phase 1 (fixture + license)
        └──> Phase 2 (probe → record → assert)
                   └──> Phase 3 (threshold decision; needs the probe score)
                              └──> Monstera re-verify (immediately after any seeding)
Phase 4 (subtitle)  ─┐  independent of 1–3 — land FIRST to de-risk while GMD probe is set up
Phase 5 ((0%) chips) ─┘  independent of 1–3 — parallelizable
        └──> Phase 6 (gates) requires ALL of 1–5
```

- **Hard dependency:** Phases 2–3 cannot start before the fixture exists (Phase 1). The assertion form
  (Phase 2's last task) and the threshold decision (Phase 3) are both **blocked on the probe score** —
  do not pre-commit the strong assertion or an empty/non-empty map before the number is measured.
- **Seed → immediately re-verify Monstera.** Do not defer the cross-class check to Phase 6; catching a
  `ModelScoreMapper` regression late is the expensive failure mode.
- **Parallelize the UX fixes.** Phases 4–5 touch strings/UI only and share no files with 1–3; do them
  first to de-risk the sprint while the GMD probe is being set up.
- **Conditional cleanups** sit *inside* Phases 3 and 5 and only activate if their trigger fires.
- **Close-out runs cheap-to-expensive:** focused JVM tests → instrumented probe → full suite →
  stub-isolation / network-free / integration-flow gates → ledger flip. Don't flip the ledger until
  every gate is green.

## UX fix B decision (the merge's call)

The user left the direction open; all three independent drafts (codex, gemini, claude) converged on
dropping the suffix (gemini initially floated `<1%`, but its own and the cross-critiques rejected it
as float-plumbing that breaks the no-new-infra boundary). `probabilityPct` is an
already-floored `Int` (`LowConfidencePickerViewModel.kt`, `coerceIn(0,100)`); the screen renders
`"… (${c.probabilityPct}%)"` in `LowConfidencePickerScreen.kt`. On a degenerate/black capture, tiny
non-zero scores floor to `0`, producing "Jade plant (0%)" — reads as broken.

- **Display `<1%`** — most informative, but only honest if we can distinguish a true 0 from a
  rounded-down 0, and the screen only has the floored `Int`. Doing it honestly means plumbing the
  pre-floor float through the ViewModel — **new data flow, which violates the no-new-infrastructure
  boundary** — and would mislabel a genuine 0 as `<1%`. **Rejected.**
- **Drop the `(x%)` suffix when it floors to 0 — show just the name. ✅ CHOSEN.** Smallest, safest change
  (one chip-text branch), no data plumbing, keeps the candidate fully selectable, removes the only thing
  that reads as broken. The percentage was never the point of these chips — selection is.
- **Suppress chips below a visibility threshold** — rejected: this is a *picker*; hiding a candidate
  removes something the user might legitimately tap, trading a cosmetic glitch for lost function.

## Risks & mitigations

- **R1 — Crassula top-1 below 0.55 or mis-ranked.** Jade may be a weaker AIY V1/3 class than Monstera.
  *Mitigation:* the threshold mechanism already exists and is tested — seed a per-class override
  (Phase 3) and downgrade to the documented honest assertion form, recording the number; never weaken
  silently. A clean, well-lit canonical fixture (Phase 1) reduces the odds.
- **R2 — Fixture ambiguity / wrong top-1 class.** A flowering or mixed-succulent jade photo could
  resolve to a neighbouring class. *Mitigation:* pick a canonical jade image; if the first fixture
  probes poorly *for image reasons*, swap the photo (cheap) **before** touching thresholds.
- **R3 — Cross-class threshold contamination.** Adding the *first-ever* `per_species_thresholds` entry
  could change behaviour for `monstera-deliciosa` (override-then-global in `ModelScoreMapper`).
  *Mitigation:* the Phase 3 Monstera re-verify task, run immediately after seeding; plus the threshold
  contract tests.
- **R4 — License / attribution slip.** Wrong or missing CC attribution. *Mitigation:* copy the 0005
  Monstera provenance block structure verbatim; record exact source page, author, retrieved date, and
  the required attribution string; keep the asset test-only (not in APK).
- **R5 — Subtitle rewrite reds the suite.** `LowConfidencePickerSubtitleContractTest` pins the copy.
  *Mitigation:* Phase 4's second task updates it in the same change.
- **R6 — `(0%)` fix breaks test-tag matching / selectability.** *Mitigation:* presentational-only change;
  preserve node identity and `testTagsAsResourceId` tags; assert both presence and clickability.
- **R7 — Scope creep via the conditional cleanups.** *Mitigation:* both are gated; if the trigger
  (third knob on `FakeFixedIdentifier`; real seeding forcing margin-semantics) does not fire, the
  deliverable is a logged note, not code.
- **R8 — GMD / probe flakiness or environment drift.** *Mitigation:* reuse the green
  `OnDeviceModelRealInterpreterTest` harness and `pixel6Api34`; re-run via `scripts/integration-flow.ps1`
  cold/warm/buildonly before close-out.

## Acceptance criteria / done-ness

- [ ] `app/src/androidTest/assets/identify-fixtures/crassula-ovata.jpg` is checked in (matching the
      Monstera fixture's dims/encoding), with a complete provenance block appended to `LICENSE.txt`
      (CC license + full attribution + test-only note).
- [ ] `OnDeviceModelRealInterpreterTest` runs the crassula probe on `pixel6Api34` and carries a passing
      accuracy assertion — preferred form if the global is cleared cleanly, else the documented honest
      fallback with a recorded reason.
- [ ] Probe outcome (top-1, top-3, score, route, source) recorded in **both** `docs/kb/ml-mapping-notes.md`
      and `docs/sprints/results/PLANTPOTTING-0006.md`, mirroring the Monstera entry.
- [ ] `per_species_thresholds` decision recorded with the deciding number — seeded with a value **or**
      explicitly "not warranted, ships empty" — and `model_manifest.json` reflects it.
- [ ] If seeded: the Monstera assertion re-runs green immediately after seeding, proving no cross-class
      regression; threshold contract tests updated.
- [ ] `R.string.low_conf_subtitle` contains no "model" jargon and reads as the new copy;
      `LowConfidencePickerSubtitleContractTest` updated and green.
- [ ] No chip renders "(0%)"; floored-to-zero candidates show the name with no suffix and remain
      selectable; `LowConfidencePickerScreenTest` covers both the zero-suffix and normal-suffix cases.
- [ ] Full JVM suite + `LowConfidencePickerScreenTest` + instrumented Monstera & crassula tests pass;
      `scripts/integration-flow.ps1` cold/warm/buildonly and the stub-isolation / network-free gates are
      GREEN.
- [ ] Any conditional cleanup either implemented (because its trigger fired) or explicitly logged as
      "not forced — deferred" in the mapping notes. No unrelated refactors landed.
- [ ] V0.1 multi-species sweep declared complete in the results doc and the ledger flipped.
