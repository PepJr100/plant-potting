# PLANTPOTTING-0006 — sprint plan (CLAUDE draft)

> Extend the proven 0005 probe harness to the second and final in-vocab species,
> `crassula-ovata` (jade plant), completing the V0.1 multi-species sweep, and clear the
> two UX findings from the 0005 review. Deliberately narrow — accuracy evidence is
> expensive, so we reuse every 0005 mechanism and add zero infrastructure.

## 1. Goals

- **G1 — Real-photo evidence for `crassula-ovata`.** Source/bundle a CC-licensed jade-plant
  fixture, run the on-device GMD probe, and record top-1/top-3/score/route/source, mirroring
  the Monstera entry. This is the last of the two AIY V1/3 ↔ KB overlaps; landing it completes
  the V0.1 multi-species sweep.
- **G2 — CI-guarded accuracy assertion for `crassula-ovata`** in `OnDeviceModelRealInterpreterTest`,
  preferring the strong form (`speciesId == "crassula-ovata" && !lowConfidence`), with an honest
  fallback only if the probe data demands it.
- **G3 — Evidence-driven `perSpeciesThresholds` decision.** Seed the map *only* if the probe shows
  a per-class override is justified; record the decision either way. This is the first real chance
  the map gains an entry.
- **G4 — UX fix A:** de-jargon `R.string.low_conf_subtitle` (drop "model").
- **G5 — UX fix B:** fix the degenerate `(0%)` chip rendering, with the direction recommended below
  and covered in `LowConfidencePickerScreenTest`.

## 2. Non-goals (do not plan)

Model swap / new model; delegate or quantization variants (INT8/GPU/NNAPI); net-new screens
(`CaptureFailedScreen`, `Settings`, `ModelInfoScreen`); ML training or retraining; changes to the
`PlantIdentifier` / `IdentificationResult` interfaces (frozen, 0003 §4.4); KB edits to
`species.json` / `archetypes.json` / `plant_class_map.json`; AGP/Kotlin/Compose/Hilt/TFLite version
bumps; README rewrite (append-only); `expected-artifacts` re-baselining; any net-new effort on the
`CameraUiState.Failure` live visual (user-waived — JVM coverage accepted).

## 3. Scope boundaries

- **Reuse, don't build.** Fixture + provenance pattern (`identify-fixtures/` +
  `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`), the
  `OnDeviceModelRealInterpreterTest` probe harness, and the `perSpeciesThresholds` mechanism
  (`model_manifest.json` `per_species_thresholds` → `ModelManifestReader` → `ModelScoreMapper`
  override-then-global at `ModelScoreMapper.kt:55-66`). No new test classes, no new assets dirs,
  no new manifest fields.
- **Two adjacent-cleanup items are *conditional*, not planned work.** Each is gated on the crassula
  data actually forcing it (see §4 Phase 3). If the data does not force them, the deliverable is a
  one-paragraph "logged, not built" note — nothing more.
- **One photo, one species.** No speculative probing of out-of-vocab species; the other 14 KB
  species are not in the model vocabulary and route to `LowConfidencePicker` by design.

## 4. Task list (every concrete work item is a checkbox)

### Phase 1 — Fixture & provenance

- [ ] Select a `crassula-ovata` (jade plant) photo on Wikimedia Commons under CC-BY-SA / CC-BY /
      CC0, depicting an unambiguous jade plant (fleshy oval leaves, woody stem; avoid flowering
      close-ups or mixed-succulent arrangements that could confuse top-1).
- [ ] Centre-crop and scale to **480×480**, re-encode **JPEG quality 80**, to match the Monstera
      fixture's dimensions/encoding exactly; save as
      `app/src/androidTest/assets/identify-fixtures/crassula-ovata.jpg`.
- [ ] Append a `crassula-ovata.jpg` provenance block to
      `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`, following the 0005 Monstera
      block verbatim in structure (Title / Depicts / Source page / Original file / Author / Date
      taken / Retrieved / Modifications / License + attribution string + test-only/not-in-APK note).
- [ ] Confirm the asset is under `app/src/androidTest/` only (test-only; not bundled into the
      production APK) and that the merged-assets copy picks it up on the next build.

### Phase 2 — Probe, record, assert

- [ ] Add a probe path for the crassula fixture to `OnDeviceModelRealInterpreterTest` that loads
      `identify-fixtures/crassula-ovata.jpg`, runs the real `OnDevicePlantIdentifier.identify(...)`,
      and surfaces top-1 id, top-1 score, the top-3 candidates, `lowConfidence`, and `source`.
- [ ] Run the test on the `pixel6Api34` Gradle Managed Device and capture the raw probe numbers
      (top-1/top-3/score/route/source) into the sprint evidence trail.
- [ ] Record the crassula probe outcome in `docs/kb/ml-mapping-notes.md` under the existing
      `Crassula ovata → crassula-ovata` section, mirroring the Monstera "Probe outcome" paragraph
      (measured top-1 id + p, lowConfidence, source, other mapped candidates, route taken).
- [ ] Record the same outcome in the sprint results doc
      (`docs/sprints/results/PLANTPOTTING-0006.md`) so the multi-species sweep evidence is
      co-located with the 0005 Monstera result.
- [ ] Commit the accuracy-bearing assertion in `OnDeviceModelRealInterpreterTest`:
      **preferred form** `speciesId == "crassula-ovata" && lowConfidence == false && source == ON_DEVICE_MODEL`
      if the probe clears the 0.55 global cleanly. If — and only if — the probe data requires it,
      commit the documented honest fallback instead and record *why* in the test comment + mapping
      notes (no silent weakening).

### Phase 3 — `perSpeciesThresholds` decision (evidence-gated)

- [ ] Decide whether `crassula-ovata` warrants a `per_species_thresholds` entry: justified **only**
      if top-1 is the correct class but its score falls below the 0.55 global by a margin an
      absolute per-class override would cleanly close (per `model_manifest.json`
      `_comment_per_species_thresholds`). If it clears the global, the map stays empty by design.
- [ ] If justified: seed `per_species_thresholds["crassula-ovata"] = <value>` in
      `app/src/main/assets/ml/aiy_plants_v1/model_manifest.json`, and confirm `ModelManifestReader`
      parses it and `ModelScoreMapper` applies override-then-global. Otherwise leave the map empty.
- [ ] Record the seeding decision (seeded-with-value **or** not-warranted, with the deciding number)
      in `docs/kb/ml-mapping-notes.md` regardless of outcome.
- [ ] **Conditional — only if real crassula seeding forces it:** revisit the `perSpeciesThresholds`
      top-1-only override semantics (margin path `high_confidence_margin_min` /
      `high_confidence_margin_delta`, and lower-ranked-mapped-candidate-when-top-1-unmapped). If the
      data does not force a decision, log the open question in the mapping notes and do **not** build
      it.

### Phase 4 — UX fix A (subtitle jargon)

- [ ] Rewrite `R.string.low_conf_subtitle` in `app/src/main/res/values/strings.xml` to drop the word
      "model". Use: **"We're best at common houseplants — confirm or pick from the list below."**
- [ ] Update/confirm any test asserting the old subtitle copy (search `low_conf_subtitle` usages) so
      the suite reflects the new string.

### Phase 5 — UX fix B (`(0%)` chips)

- [ ] Implement the **recommended** behaviour (see §6): when a candidate's `probabilityPct` floors to
      `0`, render the chip as the name **with no `(x%)` suffix** (e.g. "Jade plant"), instead of
      "Jade plant (0%)". Change is localized to the chip text builder in
      `LowConfidencePickerScreen.kt:81`; keep the suffix for all `>= 1%` candidates unchanged.
- [ ] Keep the candidate **selectable** — fix is presentational only; do not drop the chip (a picker
      exists so the user can choose; a zero-score candidate must remain pickable).
- [ ] Add/extend `LowConfidencePickerScreenTest` to cover: (a) a `0%` candidate renders the name with
      **no** `(…%)` suffix and is still present/clickable, and (b) a normal candidate (e.g. `(72%)`)
      still renders its suffix. Use the existing `testTagsAsResourceId` bridge for node matching.

### Phase 6 — Close-out gates

- [ ] Run the full JVM unit suite + `LowConfidencePickerScreenTest` + the instrumented
      `OnDeviceModelRealInterpreterTest` (Monstera **and** crassula) on `pixel6Api34`; all green.
- [ ] Run `scripts/integration-flow.ps1` (cold / warm / buildonly) and confirm the stub-isolation /
      network-free gates remain GREEN.
- [ ] Flip the sprint ledger entry for PLANTPOTTING-0006 and note V0.1 multi-species sweep as
      complete in the results doc.

## 5. Sequencing & dependencies

```
Phase 1 (fixture+license)
        └──> Phase 2 (probe → record → assert)
                   ├──> Phase 3 (threshold decision; needs the probe score)
                   └──> drives the assertion form in Phase 2's last task
Phase 4 (subtitle)  ─┐  independent of 1–3; can land in parallel
Phase 5 ((0%) chips) ─┘  independent of 1–3; can land in parallel
        └──> Phase 6 (gates) requires ALL of 1–5
```

- **Hard dependency:** Phases 2 and 3 cannot start before the fixture exists (Phase 1), and the
  assertion form (Phase 2) + the threshold decision (Phase 3) are both **blocked on the probe score**.
  Do not pre-commit either the strong assertion or an empty/non-empty map before the number is in hand.
- **Parallelizable:** Phases 4 and 5 touch UI/strings only and share no files with 1–3; they can be
  done first to de-risk the sprint while the GMD probe is set up.
- **Conditional cleanups** (FakeFixedIdentifier split, threshold-semantics revisit) sit *inside*
  Phases 5 and 3 respectively and only activate if their trigger fires.

## 6. UX fix B — recommendation & trade-off (merge decides)

`probabilityPct` is an already-floored `Int` (`LowConfidencePickerViewModel.kt:67,87`,
`coerceIn(0,100)`); the screen renders `"… (${c.probabilityPct}%)"` at `LowConfidencePickerScreen.kt:81`.
On a degenerate/black capture, tiny non-zero scores floor to `0`, producing "Jade plant (0%)" — reads
as broken. Three options:

- **(a) Display `<1%`.** Most informative, but *only correct if we can distinguish a true zero from a
  rounded-down zero* — and at the screen we only have the floored `Int`. Doing this honestly means
  plumbing the pre-floor float from the candidate string through the ViewModel, i.e. new data flow.
  That conflicts with the "no new infrastructure / narrow" mandate, and would mislabel a genuine 0 as
  `<1%`.
- **(b) Drop the `(x%)` suffix when it floors to 0 — show just the name. ← RECOMMENDED.** Smallest,
  safest change (one chip-text branch), no data plumbing, keeps the candidate fully selectable, and
  removes the only thing that reads as broken (the literal "(0%)"). The percentage was never the point
  of these chips — selection is.
- **(c) Suppress chips below a visibility threshold.** Rejected: this is a *picker*; hiding a candidate
  removes a thing the user might legitimately want to tap, trading a cosmetic glitch for lost function.

**Recommendation: (b).** It is the minimal, honest, infrastructure-free fix that preserves the picker's
purpose. (Gemini's draft recommends (a); the merge should weigh the float-plumbing cost of (a) against
the simplicity of (b).)

## 7. Risks & mitigations

- **R1 — Crassula top-1 scores below 0.55 (or mis-ranks).** The jade plant may be a weaker AIY V1/3
  class than Monstera. *Mitigation:* the threshold mechanism already exists and is tested — seed a
  per-class override (Phase 3) and downgrade to the documented honest assertion form, recording the
  number. Do not weaken silently. Choosing a clean, well-lit fixture (Phase 1) reduces the odds.
- **R2 — Fixture ambiguity / wrong top-1 class.** A flowering or mixed-succulent photo could resolve to
  a neighbouring class. *Mitigation:* select a canonical jade-plant image; if the first fixture probes
  poorly *for image reasons*, swap the photo (cheap) before touching thresholds.
- **R3 — License/attribution slip.** Wrong or missing CC attribution. *Mitigation:* copy the 0005
  Monstera provenance block structure verbatim; record exact source page, author, retrieved date, and
  the required attribution string; keep the asset test-only (not in APK).
- **R4 — `(0%)` fix changes accessibility/test-tag matching.** *Mitigation:* presentational-only change;
  preserve node identity and `testTagsAsResourceId` tags so the candidate stays matchable/clickable;
  assert both presence and clickability in the test.
- **R5 — Scope creep via the conditional cleanups.** *Mitigation:* both are gated; if the trigger
  (third knob on `FakeFixedIdentifier`; real seeding forcing margin-semantics) does not fire, the
  deliverable is a logged note, not code.
- **R6 — GMD/probe flakiness or environment drift.** *Mitigation:* reuse the green
  `OnDeviceModelRealInterpreterTest` harness and `pixel6Api34`; re-run via
  `scripts/integration-flow.ps1` cold/warm/buildonly before close-out.

## 8. Acceptance criteria / done-ness

- [ ] `app/src/androidTest/assets/identify-fixtures/crassula-ovata.jpg` is checked in at 480×480 / JPEG
      q80, with a complete provenance block appended to `LICENSE.txt` (CC license + attribution +
      test-only note).
- [ ] `OnDeviceModelRealInterpreterTest` runs the crassula probe on `pixel6Api34` and carries a passing
      accuracy assertion — preferred form if the global is cleared cleanly, else the documented honest
      fallback with a recorded reason.
- [ ] Probe outcome (top-1, top-3, score, route, source) recorded in **both** `docs/kb/ml-mapping-notes.md`
      and `docs/sprints/results/PLANTPOTTING-0006.md`, mirroring the Monstera entry.
- [ ] `per_species_thresholds` decision recorded with the deciding number — seeded with a value **or**
      explicitly "not warranted, ships empty" — and `model_manifest.json` reflects it.
- [ ] `R.string.low_conf_subtitle` contains no engineer jargon ("model"); reads as the new copy.
- [ ] No chip renders "(0%)"; floored-to-zero candidates show the name with no suffix and remain
      selectable; `LowConfidencePickerScreenTest` covers both the zero-suffix and normal-suffix cases.
- [ ] Full JVM suite + `LowConfidencePickerScreenTest` + instrumented Monstera & crassula tests pass;
      `scripts/integration-flow.ps1` cold/warm/buildonly and the stub-isolation / network-free gates
      are GREEN.
- [ ] Any conditional cleanup either implemented (because its trigger fired) or explicitly logged as
      "not forced — deferred" in the mapping notes. No unrelated refactors landed.
- [ ] V0.1 multi-species sweep declared complete in the results doc and the ledger flipped.
