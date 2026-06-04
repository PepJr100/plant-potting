# PLANTPOTTING-0007 — Houseplant model swap (V1 entry)

PLANTPOTTING-0006 closed V0.1 by proving the calibration plumbing is sound — and proving the
model is the bottleneck. AIY Plants V1/3 is houseplant-blind: only **2 of 16** KB species are
in-vocab, jade (`crassula-ovata`) routes low-confidence at **0.1055** on a clean canonical
fixture, and *Monstera deliciosa* (0.8984) is the lone strong result. This sprint opens **V1**
by replacing the model path with an on-device, network-free classifier weighted toward common
houseplants — and it must **land a running prototype**, not a paper spike: the winning model
wired into the app (behind a flag/branch) identifying plants live through the production
`OnDevicePlantIdentifier` path.

The work is grounded in reusable 0003–0006 infra: `OnDeviceModelRealInterpreterTest` (the only
test that drives the real interpreter end-to-end), `ModelManifest` + `ModelManifestReader`,
`ModelScoreMapper` (override-then-global), `ModelLabelMap`/`ModelLabelMapReader`,
`ImagePreprocessor` (dtype-branching UINT8/FLOAT32), and the
`app/src/main/assets/ml/<model_id>/` bundle convention (`model.tflite` + `labels.csv` +
`model_manifest.json` + `plant_class_map.json` + license file).

## Goals

- **G1 — Survey + decision matrix.** Survey on-device, network-free, bundle-friendly candidate
  classifiers with houseplant-weighted vocabularies; produce a decision matrix scoring each on
  KB-vocabulary overlap, size, conversion risk, and license (license is **reported, not gated**).
- **G2 — Expanded real-photo fixture set.** Grow `identify-fixtures/` beyond Monstera + jade to
  cover several more of the 16 KB species (snake plant, pothos, ZZ, peace lily, + 2 more), with
  full CC provenance, matching the existing 480×480 / JPEG-q80 convention.
- **G3 — Swap-evaluation harness.** A repeatable harness that probes the AIY baseline **and** each
  shortlisted candidate over the expanded fixtures, emitting a machine-readable per-fixture report
  (raw top-1/top-3, mapped KB id, in-vocab flag, score, route, source) plus a human summary
  comparing top-1/top-3 and in-vocab coverage against the AIY baseline.
- **G4 — Running prototype (the headline).** Wire the winning model into the app behind a
  `BuildConfig`/flag (or dedicated branch) so a live device/emulator run identifies real
  houseplant fixtures directly through `OnDevicePlantIdentifier`, returning `ON_DEVICE_MODEL`.
- **G5 — Re-baseline.** Author the winning model's `plant_class_map.json` (with `_comment_coverage`
  recomputed from its actual vocabulary) and `model_manifest.json` `per_species_thresholds`
  (seeded only from fixture evidence near the global, never to bless ~10% predictions).
- **G6 — Constraints preserved.** `verifyNoNetworking` and `check-stub-isolation.sh` stay GREEN;
  the `PlantIdentifier`/`IdentificationResult` seam (0003 §4.4) stays unchanged **by default**.

## Non-goals / scope boundaries

- [ ] **No production networking.** No cloud ID, API calls, remote model download at runtime,
      telemetry, or Firebase ML. Any model-acquisition/conversion script lives under
      `docs/sprints/evidence/PLANTPOTTING-0007/` or `scripts/`, is **never** invoked from a Gradle
      app task, and does not run on device — `verifyNoNetworking` must stay GREEN.
- [ ] **No ML training/retraining as the primary path.** We find/evaluate/integrate pre-trained
      TFLite classifiers. (A 16-class KB-specific head is an explicit *last-resort fallback* only —
      see Phase 1 — and if used must be labelled "project-trained", not an upstream model.)
- [ ] **No net-new screens** (`CaptureFailedScreen`, `Settings`, `ModelInfoScreen`, model picker UI).
      The prototype flag is a `BuildConfig`/constant or branch, not user-facing chrome.
- [ ] **No KB edits.** `species.json` / `archetypes.json` are locked; only the new model's
      `plant_class_map.json` mapping + coverage comment change.
- [ ] **No recommendation-logic changes.** Success is *better identification* feeding the existing
      KB-driven recipe path unchanged.
- [ ] **`PlantIdentifier` / `IdentificationResult` seam frozen by default** (0003 §4.4). Changing it
      requires a strong, documented justification in the results doc + updating every caller/test
      in the same phase.
- [ ] **No AGP/Kotlin/Compose/Hilt/TFLite version bumps** unless a selected TFLite candidate
      genuinely cannot run on the current stack; if so, document it as explicit blocker-removal.
- [ ] **No `expected-artifacts` re-baselining** unless the prototype intentionally changes
      integration output, with the reason recorded.
- [ ] **Do not delete the AIY bundle** until the replacement is wired, tested, and evidence-backed.
      AIY stays as the baseline asset (and the default flag target) through close-out.
- [ ] **Do not lower thresholds to make a weak model look good.** Routing honesty is non-negotiable.

## Candidate models to evaluate

Named starting points — Phase 1 confirms availability/redistribution before any download:

- [ ] **`aiy_plants_v1` (baseline, frozen).** Carry its known numbers into every report: Monstera
      0.8984 high-conf, jade 0.1055 low-conf, 2/16 KB species in-vocab. The control column.
- [ ] **PlantNet-300K MobileNetV3-Small INT8** (`plantnet_300k_mobilenetv3s_int8`). Primary
      candidate — small, mobile-first, plant-domain. Source: `github.com/plantnet/PlantNet-300K`
      + Zenodo weights; verify TFLite export path and label availability.
- [ ] **PlantNet-300K EfficientNet-Lite0 INT8** (`plantnet_300k_efflite0_int8`). Quality-vs-size
      comparator if MobileNetV3-Small underperforms KB coverage.
- [ ] **iNaturalist / Google "on-device plants" TFLite** (`inat_plants_tflite`) — evaluate **only**
      if a downloadable, redistributable `.tflite` + labelmap exists; otherwise record as
      reference-only, not a bundle candidate.
- [ ] **PlantCLEF-derived mobile/distilled INT8** (`plantclef_mobile_int8`) — stretch only; many
      public checkpoints are ViT-scale. Require a clean TFLite conversion (no unsupported ops) and
      bundle-plausible size before it earns a fixture probe.
- [ ] **KB16 MobileNetV3-Small head INT8** (`kb16_houseplant_mvn3s_int8`) — **last-resort fallback**
      if no public model clears the bar. Trained on licensed/open images **disjoint from the
      androidTest fixtures** (no eval-on-train), exported INT8, reported as "project-trained".

## Phase 1 — Candidate survey & decision matrix

- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0007/model-candidate-matrix.md` with one row per
      candidate above (incl. AIY baseline).
- [ ] For each candidate record: source URL, weight availability, **redistribution status**
      (dataset-public ≠ weights-redistributable), labels availability, input size + dtype, output
      shape, label count, expected preprocessing, est. compressed & uncompressed size, conversion
      risk (already-TFLite vs needs-conversion / unsupported-ops risk), and license.
- [ ] For each candidate, compute vocabulary overlap against the **16 KB species** in
      `app/src/main/assets/kb/species.json`, honouring the aliases already encoded in the AIY
      `plant_class_map.json` (e.g. `Sansevieria trifasciata` → `dracaena-trifasciata`,
      `Calathea orbifolia` → `goeppertia-orbifolia`). Report overlap as "M of 16 KB species".
- [ ] Record licensing as a **reported axis, not a gate**: license name, attribution requirement,
      commercial-use note, weight-redistribution status, dataset-vs-weight ambiguity.
- [ ] Shortlist **at most 2** candidates for full fixture probing (default: PlantNet-300K
      MobileNetV3-Small INT8 + one of {best ready-made public TFLite, KB16 fallback}). Record the
      shortlist rationale in the matrix.

## Phase 2 — Expanded real-photo fixture set

- [ ] Audit `app/src/androidTest/assets/identify-fixtures/`: keep `monstera-deliciosa.jpg` and
      `crassula-ovata.jpg` **byte-unchanged** so AIY baseline comparisons stay stable.
- [ ] Source a canonical CC/CC0 `dracaena-trifasciata` (snake plant) fixture — upright banded
      leaves; avoid mixed planters / cultivar-only close-ups.
- [ ] Source a canonical CC/CC0 `epipremnum-aureum` (pothos) fixture — heart leaves + vine habit;
      avoid philodendron/scindapsus ambiguity.
- [ ] Source a canonical CC/CC0 `zamioculcas-zamiifolia` (ZZ) fixture — glossy pinnate leaflets +
      stems; avoid cropped single-leaf shots.
- [ ] Source a canonical CC/CC0 `spathiphyllum-wallisii` (peace lily) fixture — foliage and/or
      spathe; avoid generic-aroid close-ups.
- [ ] Source **2 more** KB-species fixtures to broaden coverage, preferring non-aroid:
      `ficus-lyrata`, `chlorophytum-comosum`, `phalaenopsis`, or `goeppertia-orbifolia`.
- [ ] Centre-crop + scale + re-encode each new fixture to match the existing convention
      (**480×480, JPEG q80**); confirm dims/encoding with `file(1)` against `monstera-deliciosa.jpg`.
- [ ] Append a complete provenance block per new fixture to
      `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`, mirroring the Monstera/jade blocks
      verbatim in structure (Title / Depicts / Source page / Original file / Author / Date taken /
      Retrieved / Modifications / License + attribution string + test-only/not-in-APK note).
- [ ] Confirm all new fixtures live under `app/src/androidTest/` **only** (not in the production
      APK), and add a fixture-readability guard in the harness so a missing/corrupt/misnamed image
      fails loudly before inference.

## Phase 3 — Swap-evaluation harness

- [ ] Introduce a test-only `ModelUnderTest` descriptor (model id, manifest asset path, labels
      asset, mapping asset, model asset path, input size/dtype) so the harness can drive AIY +
      candidate bundles without duplicating interpreter/preprocessor/mapper code. Reuse
      `TfLiteInterpreterFacade`, `ImagePreprocessor`, `ModelScoreMapper`, `ModelLabelMapReader`.
- [ ] Add candidate bundles under `app/src/main/assets/ml/<candidate_id>/` (`model.tflite`,
      `labels.csv`, `model_manifest.json`, `plant_class_map.json`, license file) **only** for a
      candidate that is bundle-ready and will be probed/wired. If a candidate is too large or its
      weights aren't redistributable, keep the conversion script + evidence under
      `docs/sprints/evidence/PLANTPOTTING-0007/`, mark it "not bundle-ready", and exclude it from
      the running prototype.
- [ ] Build the harness as a dedicated GMD test (`ModelSwapEvaluationTest`) **alongside**, not
      replacing, `OnDeviceModelRealInterpreterTest` — keep the existing Monstera/jade/binding
      assertions intact as the regression anchor.
- [ ] Iterate the harness over `{baseline AIY} × {expanded fixtures}` and emit a machine-readable
      report at `docs/sprints/evidence/PLANTPOTTING-0007/model-swap-eval.csv` with columns: model
      id, fixture species id, expected species id, raw top-1 label, raw top-1 score, raw top-3
      labels+scores, mapped top-1 KB id, mapped top-3 KB ids, in-vocab flag, route (high/low-conf),
      source, failure reason (if any).
- [ ] Iterate the harness over the **shortlisted candidate(s)** × the same fixtures into the same
      CSV, so AIY and candidates are directly comparable row-for-row.
- [ ] Add a regression assertion pinning the **AIY baseline rows** (Monstera high-conf @ ~0.8984,
      jade low-conf @ ~0.1055) so the harness can't silently mask a plumbing regression.
- [ ] Add **evidence-driven** candidate assertions *after* the probe (do not pre-commit a form):
      preferred = correct top-1 high-confidence for the required fixtures; acceptable documented
      fallback = correct species in top-3 with a recorded threshold/mapping reason.
- [ ] Write the human summary at
      `docs/sprints/evidence/PLANTPOTTING-0007/model-swap-eval-summary.md` comparing AIY vs
      candidates on top-1 count, top-3 count, in-vocab KB coverage, mean correct-class score, asset
      size delta, and license status — and name the winner with rationale.

## Phase 4 — Winning-model manifest & mapping (re-baseline)

- [ ] Author `app/src/main/assets/ml/<winning_model>/model_manifest.json`: source URL, variant,
      sha256, `placeholder=false`, input size, input dtype, color order, output tensor shape, label
      count, acquisition date, license, license file, labels asset, mapping asset, `thresholds`,
      `per_species_thresholds`, `_comment_coverage`. (Mirror the AIY manifest's comment discipline.)
- [ ] Author `app/src/main/assets/ml/<winning_model>/plant_class_map.json`: map every KB species
      present in the winner's vocabulary to the correct `kbSpeciesId`, including `alias: true` rows
      where the model uses older/synonym names (re-derive aliases — don't assume AIY's set carries).
- [ ] Recompute `_comment_coverage` from the winner's **actual** labels and state both
      mapping-entry overlap and KB-species overlap (e.g. "N of 18 entries, M of 16 KB species") —
      the headline coverage delta vs AIY's 2/16.
- [ ] Re-baseline `per_species_thresholds` from the expanded fixture scores: seed an override
      **only** where the correct mapped class sits near the global and an absolute override cleanly
      closes the gap; never lower thresholds to bless ~10% predictions. Record each decision (+ the
      deciding number) — including "stays empty by design" if so.
- [ ] Add a PLANTPOTTING-0007 section to `docs/kb/ml-mapping-notes.md`: winning vocabulary, alias
      decisions, per-fixture outcomes, coverage change from AIY, per-species threshold decisions,
      and the license report.
- [ ] Update the affected JVM tests to the winner's bundle: `ModelManifestTest`,
      `ModelAssetsPresenceTest`, `ModelLabelMappingValidationTest`, `PerSpeciesThresholdsContractTest`,
      `ModelScoreMapperPerSpeciesThresholdTest`, `ModelManifestDtypeContractTest` — adjusting for the
      winner's dtype/coverage/thresholds (and keeping AIY-bundle coverage if AIY stays as the
      default flag target).

## Phase 5 — Running-prototype wiring (G4 — the headline)

- [ ] Add a single model-root selection point: a `BuildConfig` field (or top-level constant)
      `ACTIVE_MODEL_ROOT` defaulting to `ml/aiy_plants_v1`, flipped to `ml/<winning_model>` on the
      prototype build/branch. One switch — no scattered conditionals.
- [ ] Refactor `OnDeviceIdentifyProvidersModule` (`OnDeviceIdentifyModule.kt`) so
      `provideModelManifest`, `provideModelLabels`, `provideModelLabelMap`, and
      `provideInterpreterFacade` read from `ACTIVE_MODEL_ROOT` instead of hardcoding
      `ml/aiy_plants_v1/model.tflite` and the AIY manifest path. (`ModelManifestReader`/
      `ModelLabelsReader`/`ModelLabelMapReader` already take asset paths — thread the root through.)
- [ ] Keep `PlantIdentifier.identify(jpeg): IdentificationResult` and `IdentificationResult`
      **unchanged**. Confine all tensor-shape/dtype/preprocessing differences inside the
      interpreter/preprocessor layer behind the seam. If the winner genuinely can't fit the seam,
      stop and record the exact reason in the results doc before changing it, then update all
      callers/tests in this phase.
- [ ] Confirm `OnDevicePlantIdentifier` still returns `IdSource.ON_DEVICE_MODEL` for the winner and
      still routes weak/out-of-vocab predictions to the existing low-confidence flow (picker
      unchanged).
- [ ] Add/extend an instrumentation test that drives the **app-wired winning model** through
      `OnDevicePlantIdentifier.identify(...)` on at least Monstera, jade, snake plant, pothos, ZZ,
      and peace lily fixtures (the production path, not the bare harness).
- [ ] Perform a **live smoke run** on `pixel6Api34` (or device) with the flag/branch enabled and
      capture evidence (logcat/screenshot) that the app identifies **≥3 houseplant fixtures
      directly** through the production identifier path. This is the "not a paper spike" gate.

## Phase 6 — Constraint, size & integration gates (cheap → expensive)

- [ ] Focused JVM tests first: manifest parsing, label mapping, score mapping, per-species
      thresholds, and `VerifyNoNetworkingRegressionTest` — all green.
- [ ] `verifyNoNetworking` GREEN after candidate deps + wiring land (fix any new network surface
      before proceeding).
- [ ] `scripts/check-stub-isolation.sh` GREEN — the production swap must not leak fake/test
      identifiers into app wiring.
- [ ] Measure **asset/APK size impact**: raw winner model size, compressed APK delta vs AIY, and
      any `noCompress`/`aaptOptions` implication; record in `model-swap-eval-summary.md`.
- [ ] Instrumented tests on `pixel6Api34`: `OnDeviceModelRealInterpreterTest` (AIY baseline rows
      still green), `ModelSwapEvaluationTest`, and the Phase 5 app-wired prototype test.
- [ ] Full `testDebugUnitTest` + `ktlintCheck` GREEN.
- [ ] `scripts/integration-flow.ps1` cold / warm / buildonly — record whether expected artifacts
      stay unchanged or why a re-baseline is intentionally required.
- [ ] Write `docs/sprints/results/PLANTPOTTING-0007.md`: selected model, rejected candidates +
      why, fixture results table, top-1/top-3 + coverage delta vs AIY, threshold changes, license
      report, size report, **live-prototype status (with evidence link)**, and gate results. Flip
      the ledger entry only after every gate above is green.

## Sequencing & dependencies

```
Phase 1 (survey + matrix)  ──> shortlist (≤2 candidates)
        │                         │
        ▼                         ▼
Phase 2 (fixtures) ───────> Phase 3 (harness: baseline + candidates → CSV + summary → WINNER)
   (parallel w/ candidate conversion;          │
    fixtures are model-agnostic)               ▼
                                       Phase 4 (winner manifest + map + thresholds + tests)
                                               │
                                               ▼
                                       Phase 5 (flag wiring + app-wired test + LIVE smoke run)
                                               │
                                               ▼
                                       Phase 6 (gates: net-free/stub → size → instrumented → full → integration → results)
```

- **Phase 1 gates everything model-side** — no download/convert/check-in before license, weight
  availability, size, and KB-overlap are known.
- **Phase 2 runs in parallel** with candidate conversion once the required species list is fixed;
  fixtures are reusable regardless of which candidate wins.
- **Phase 3 must complete before Phase 5** — the prototype is backed by comparative data, not by
  whichever model loads easiest.
- **Phase 4 immediately follows winner selection** — mapping + thresholds are part of the swap, not
  later doc cleanup.
- **Phase 5 only after the winner's manifest/map are stable** so the app path and harness share the
  same assets.
- **Phase 6 cheap → expensive**: focused JVM → net-free/stub gates → size → instrumented → full
  suite + ktlint → integration-flow → results doc → ledger flip.

## Risks & mitigations

- [ ] **R1 — Public PlantNet/iNat weights aren't redistributable even though datasets/APIs are
      public.** *Mitigation:* record license + weight-availability **separately** in the matrix;
      keep the KB16 INT8 fallback in scope so a running prototype is always reachable.
- [ ] **R2 — High-accuracy checkpoint (PlantCLEF/ViT) too large or uses unsupported TFLite ops.**
      *Mitigation:* treat as stretch; require a clean conversion proof + bundle-plausible size
      *before* it earns a fixture probe; prefer MobileNetV3-Small/EfficientNet-Lite for bundle fit.
- [ ] **R3 — KB16 fallback overfits the tiny fixture set and looks better than it is.**
      *Mitigation:* train on images **disjoint** from androidTest fixtures; require top-3 +
      confidence reporting; label it "project-trained" in every report.
- [ ] **R4 — Candidate labels use synonyms/common names that break direct mapping.** *Mitigation:*
      audit labels against `species.json` aliases; encode every alias in the winner's
      `plant_class_map.json` + document in `ml-mapping-notes.md`.
- [ ] **R5 — Lowered thresholds hide a weak model behind optimistic routing.** *Mitigation:* seed
      `per_species_thresholds` only from fixture evidence near the global; keep weak predictions
      low-confidence and report them honestly (the 0006 discipline).
- [ ] **R6 — Model asset bloats the APK.** *Mitigation:* measure compressed + uncompressed deltas
      before picking the winner; prefer INT8; reject not-bundle-friendly models even if licensing
      is fine. Report the trade-off.
- [ ] **R7 — TFLite I/O shape/dtype mismatch tempts a seam break.** *Mitigation:* confine
      pre/post-processing inside the interpreter/preprocessor layer; the seam changes only with a
      documented, caller-updating justification (default: it doesn't).
- [ ] **R8 — Wiring hardcodes a candidate or deletes AIY before comparison is done.** *Mitigation:*
      single `ACTIVE_MODEL_ROOT` switch defaulting to AIY; keep AIY baseline tests alive through
      close-out.
- [ ] **R9 — A model-acquisition/conversion script smuggles in a networking path.** *Mitigation:*
      acquisition scripts live outside production runtime, are never invoked from Gradle app tasks,
      and `verifyNoNetworking` stays GREEN.
- [ ] **R10 — GMD/probe flakiness or environment drift.** *Mitigation:* reuse the green
      `pixel6Api34` GMD harness; re-run `integration-flow.ps1` cold/warm/buildonly before close-out.
- [ ] **R11 — Sprint degrades into a paper spike.** *Mitigation:* G4 + the Phase 5 live smoke run
      are acceptance-blocking — the results doc must link running-prototype evidence.

## Acceptance criteria

- [ ] `docs/sprints/evidence/PLANTPOTTING-0007/model-candidate-matrix.md` exists and reports
      availability, license, size, conversion risk, and 16-species vocabulary overlap for AIY plus
      ≥2 serious replacement candidates.
- [ ] `app/src/androidTest/assets/identify-fixtures/` contains Monstera + jade (unchanged) plus
      ≥4 new canonical fixtures (snake plant, pothos, ZZ, peace lily) + 2 more KB species — all with
      complete CC provenance in `LICENSE.txt` and all test-only (not in the APK).
- [ ] The swap-evaluation harness runs AIY **and** the shortlisted candidate(s) over the expanded
      fixtures and writes `model-swap-eval.csv` + `model-swap-eval-summary.md` comparing
      top-1/top-3, in-vocab coverage, size, and license against the AIY baseline.
- [ ] A winning replacement model is selected from evidence and has a complete bundle under
      `app/src/main/assets/ml/<winning_model>/` (or the sprint explicitly rejects all public
      candidates and lands the KB16 fallback bundle, documented as project-trained).
- [ ] The winning model is wired behind a flag/branch via a single model-root switch, and a **live
      device/emulator run** identifies ≥3 real houseplant fixtures directly through
      `OnDevicePlantIdentifier` (`source == ON_DEVICE_MODEL`) without network access — evidence
      linked from the results doc.
- [ ] `PlantIdentifier` / `IdentificationResult` remain unchanged — or the results doc carries a
      strong justification for the seam change and all callers/tests are updated in the same sprint.
- [ ] The winning model's `plant_class_map.json` (`_comment_coverage`) and `model_manifest.json`
      (`per_species_thresholds`) are re-baselined from its actual vocabulary + fixture probes, with
      every threshold decision (and deciding number) recorded.
- [ ] `docs/kb/ml-mapping-notes.md` has a PLANTPOTTING-0007 section: coverage delta, fixture
      outcomes, alias decisions, threshold decisions, and license notes.
- [ ] `verifyNoNetworking`, `scripts/check-stub-isolation.sh`, focused model/manifest/score JVM
      tests, GMD real-interpreter + swap-eval + app-wired prototype tests, full `testDebugUnitTest`,
      `ktlintCheck`, and `scripts/integration-flow.ps1` cold/warm/buildonly are GREEN — or carry a
      documented, accepted blocker in `docs/sprints/results/PLANTPOTTING-0007.md`.
- [ ] Asset/APK size impact of the winning model is measured and recorded (raw + compressed delta).
- [ ] The sprint does **not** end as a paper spike: the results doc links a running app-prototype
      state where the selected model identifies plants live behind the agreed flag/branch.
