# PLANTPOTTING-0007 — Houseplant model swap (V1 entry)

PLANTPOTTING-0006 closed V0.1 by proving the calibration plumbing is sound — and, in doing so,
proved the wrong thing about the engine: **AIY Plants V1/3 is houseplant-blind**. Only **2 of 16**
KB species are in-vocab, jade (`crassula-ovata`) routes low-confidence at **0.1055** on a clean
canonical fixture, and *Monstera deliciosa* (**0.8984**) is the lone strong result. This sprint
opens **V1 — broad species coverage** by replacing the model path with an on-device, network-free
classifier weighted toward common houseplants — and it must **land a running prototype**, not a
paper spike: the winning model wired into the app (behind a flag/branch) identifying real
houseplant photos live through the production `OnDevicePlantIdentifier` path.

The work is grounded in reusable 0003–0006 infra: `OnDeviceModelRealInterpreterTest` (the only test
that drives the real interpreter end-to-end), `ModelManifest` + `ModelManifestReader`,
`ModelScoreMapper` (override-then-global), `ModelLabelMap` / `ModelLabelMapReader`,
`ImagePreprocessor` (dtype-branching UINT8/FLOAT32), and the `app/src/main/assets/ml/<model_id>/`
bundle convention (`model.tflite` + `labels.csv` + `model_manifest.json` + `plant_class_map.json` +
license file).

## Goals

- **G1 — Survey + decision matrix.** Survey on-device, network-free, bundle-friendly candidate
  classifiers with houseplant-weighted vocabularies; produce a decision matrix scoring each on
  KB-vocabulary overlap, model size, inference latency, conversion risk, and license (license is
  **reported, not gated**).
- **G2 — Expanded real-photo fixture set.** Grow `identify-fixtures/` beyond Monstera + jade to
  cover several more of the 16 KB species (snake plant, pothos, ZZ, peace lily, + 2 more), with full
  CC provenance. *The fixtures are the ruler we measure every candidate against* — so this gates
  evaluation and is model-agnostic.
- **G3 — Swap-evaluation harness.** A repeatable harness that probes the AIY baseline **and** each
  shortlisted candidate over the expanded fixtures, feeding each fixture through that model's own
  native preprocessing, and emitting a machine-readable per-fixture report (raw top-1/top-3, mapped
  KB id, in-vocab flag, score, route, source, **inference latency**) plus a human summary comparing
  top-1/top-3, in-vocab coverage, size, and latency against the AIY baseline.
- **G4 — Running prototype (the headline).** Wire the winning model into the app behind a
  `BuildConfig`/flag (or dedicated branch) so a live device/emulator run identifies real houseplant
  fixtures directly through `OnDevicePlantIdentifier`, returning `ON_DEVICE_MODEL` — **behind the
  frozen `PlantIdentifier` / `IdentificationResult` seam (0003 §4.4) by default**, changing the seam
  only if the prototype proves a specific, documented need.
- **G5 — Re-baseline.** Author the winning model's `plant_class_map.json` (with `_comment_coverage`
  recomputed from its actual vocabulary) and `model_manifest.json` `per_species_thresholds` (seeded
  only from fixture evidence near the global — never to bless ~10% predictions).
- **G6 — Constraints preserved.** `verifyNoNetworking` and `check-stub-isolation.sh` stay GREEN
  (net-free checked **continuously**, the moment candidate deps/scripts land — not deferred to
  close-out); the swap stays bundle-friendly in size.

## Non-goals / scope boundaries

- [ ] **No production networking.** No cloud ID, API calls, runtime remote model download, telemetry,
      or Firebase ML. Any model-acquisition/conversion script lives under
      `docs/sprints/evidence/PLANTPOTTING-0007/` or `scripts/`, is **never** invoked from a Gradle app
      task, and does not run on device — `verifyNoNetworking` must stay GREEN.
- [ ] **No ML training or fine-tuning — including a KB-specific head — in this sprint.** We
      find / evaluate / integrate *pre-trained* TFLite classifiers only. If the survey finds **no**
      viable public model, the **negative result plus a recommendation to spin a dedicated
      fine-tuning sprint IS the deliverable** — do not start collecting training data or training a
      model here.
      > **Reviewed trade-off (not an oversight).** Two of the three drafts (CODEX, CLAUDE) included a
      > "KB16 project-trained head" as a *last-resort* fallback to guarantee a running prototype if no
      > public model is redistributable; the CODEX critique re-argued for it on delivery-certainty
      > grounds. It is **deliberately cut** because training a model in-sprint (licensed data sourcing,
      > disjoint-from-fixtures split, INT8 export, validation) is a different, sprint-busting
      > undertaking, and the no-public-winner case already has a concrete deliverable (negative finding
      > + fine-tuning-sprint recommendation + a demonstrated swap *mechanism*). If the principal would
      > rather guarantee a *better-model* prototype this sprint even at that cost, this is the line to
      > reopen.
- [ ] **No net-new screens** (`CaptureFailedScreen`, `Settings`, `ModelInfoScreen`, model-picker UI).
      The prototype switch is a `BuildConfig`/constant or branch, **not** user-facing chrome — no "UI
      toggle".
- [ ] **No KB edits.** `species.json` / `archetypes.json` are locked; only the new model's
      `plant_class_map.json` mapping + coverage comment change.
- [ ] **No recommendation-logic changes.** Success is *better identification* feeding the existing
      KB-driven recipe path unchanged.
- [ ] **`PlantIdentifier` / `IdentificationResult` seam frozen by default** (0003 §4.4). Changing it
      requires a strong, documented justification in the results doc **plus** updating every
      caller/test in the same phase.
- [ ] **No AGP/Kotlin/Compose/Hilt/TFLite version bumps** unless a selected TFLite candidate genuinely
      cannot run on the current stack; if so, document it as explicit blocker-removal.
- [ ] **No `expected-artifacts` re-baselining** unless the prototype intentionally changes integration
      output, with the reason recorded.
- [ ] **Do not delete the AIY bundle.** It stays as the baseline asset **and the default
      `ACTIVE_MODEL_ROOT` target** through close-out — keep its manifest/coverage tests green.
- [ ] **Do not lower thresholds to make a weak model look good.** Routing honesty is non-negotiable
      (the 0006 discipline).

## Candidate models to evaluate

Named starting points — Phase 1 confirms availability/redistribution before any download. *Licensing
and weight-redistribution status are reported in the matrix, not used as a hard gate.*

- [x] **`aiy_plants_v1` (baseline, frozen).** The control column. Carry its known numbers into every
      report: Monstera **0.8984** high-conf, jade **0.1055** low-conf, **2/16** KB species in-vocab.
- [x] **PlantNet-300K MobileNetV3-Small INT8** (`plantnet_300k_mobilenet_v3_small_int8`). Primary
      candidate — small, mobile-first, plant-domain. Source: `github.com/plantnet/PlantNet-300K` +
      Zenodo weights; verify the TFLite export path and label availability. **Two traps to record
      separately:** (a) the *dataset* being public does **not** mean the *weights* are
      redistributable; (b) a published checkpoint may be a *training* architecture (PyTorch/research
      head), not a clean mobile export — "nominally available" ≠ "converts to bundle-fit INT8 TFLite
      without unsupported ops or a retrain." Capture conversion effort, not just availability.
- [x] **PlantNet-300K EfficientNet-Lite0 INT8** (`plantnet_300k_efficientnet_lite0_int8`).
      Quality-vs-size comparator if MobileNetV3-Small underperforms KB coverage.
- [x] **iNaturalist / Google "on-device plants" TFLite** (`inat_plants_tflite`) — evaluate **only** if
      a downloadable, redistributable `.tflite` + labelmap exists; otherwise record as reference-only,
      not a bundle candidate.
- [x] **PlantCLEF-derived mobile/distilled INT8** (`plantclef_mobile_int8`) — **stretch only**; many
      public checkpoints are ViT-scale. Require a clean TFLite conversion (no unsupported ops) and a
      bundle-plausible size *before* it earns a fixture probe.

## Phase 1 — Candidate survey & decision matrix

*Why this phase first: no model should be downloaded, converted, or checked in before its license,
weight availability, size, latency profile, and KB-vocabulary overlap are known.*

- [x] Create `docs/sprints/evidence/PLANTPOTTING-0007/model-candidate-matrix.md` with one row per
      candidate above (including the AIY baseline).
- [x] For each candidate record: source URL, weight availability, **redistribution status**
      (dataset-public ≠ weights-redistributable), labels availability, input size + dtype, output
      shape, label count, expected preprocessing, est. compressed & uncompressed size, conversion risk
      (already-TFLite vs needs-conversion / unsupported-ops risk), and license.
- [x] For each candidate, compute vocabulary overlap against the **16 KB species** in
      `app/src/main/assets/kb/species.json`, honouring the aliases already encoded in the AIY
      `plant_class_map.json` (e.g. `Sansevieria trifasciata` → `dracaena-trifasciata`,
      `Calathea orbifolia` → `goeppertia-orbifolia`). Report overlap as "M of 16 KB species".
      (AIY = 2/16; PlantNet/PlantCLEF overlap not independently verifiable from public metadata —
      recorded as qualitatively low, wild-flora-weighted.)
- [x] Record licensing as a **reported axis, not a gate**: license name, attribution requirement,
      commercial-use note, weight-redistribution status, dataset-vs-weight ambiguity.
- [x] Shortlist **at most 2** candidates for full fixture probing (default: PlantNet-300K
      MobileNetV3-Small INT8 + the best ready-made public TFLite alternative). Record the shortlist
      rationale in the matrix. **If zero candidates clear the redistribution/size/conversion bar,
      record that as the survey outcome** and proceed to Phase 2–3 with whatever is probeable
      (worst case: AIY only, with a documented "no viable public swap; recommend a fine-tuning
      sprint" finding for Phase 6). → **ZERO cleared the bar; "no public winner" recorded.**

## Phase 2 — Expanded real-photo fixture set

*Why: the fixtures are the ruler. A wider, model-agnostic fixture set is the only thing that stops us
overfitting the model **choice** to the 2 species AIY happens to know (see R4).*

- [ ] Audit `app/src/androidTest/assets/identify-fixtures/`: keep `monstera-deliciosa.jpg` and
      `crassula-ovata.jpg` **byte-unchanged** so AIY baseline comparisons stay stable.
- [ ] Source a canonical CC/CC0 `dracaena-trifasciata` (snake plant) fixture — upright banded leaves;
      avoid mixed planters / cultivar-only close-ups.
- [ ] Source a canonical CC/CC0 `epipremnum-aureum` (pothos) fixture — heart leaves + vine habit;
      avoid philodendron/scindapsus ambiguity.
- [ ] Source a canonical CC/CC0 `zamioculcas-zamiifolia` (ZZ) fixture — glossy pinnate leaflets +
      stems; avoid cropped single-leaf shots.
- [ ] Source a canonical CC/CC0 `spathiphyllum-wallisii` (peace lily) fixture — foliage and/or spathe;
      avoid generic-aroid close-ups.
- [ ] Source **2 more** KB-species fixtures to broaden coverage, preferring non-aroid:
      `ficus-lyrata`, `chlorophytum-comosum`, `phalaenopsis`, or `goeppertia-orbifolia`.
- [ ] Store each new fixture at the existing canonical convention (**480×480, JPEG q80**) as the
      on-disk form; confirm dims/encoding with a **portable** check (decode in the harness and assert
      width/height/format, or `magick identify` — **not** `file(1)`, which is unreliable on this
      Windows-heavy repo) against `monstera-deliciosa.jpg`. **The fixture is the canonical capture —
      each model's *own* native input size (e.g. 224×224 for MobileNetV3) is produced by that model's
      preprocessor at probe time, not baked into the fixture.** (This keeps one fixture set comparable
      across models with different input sizes.)
- [ ] Append a complete provenance block per new fixture to
      `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`, mirroring the Monstera/jade blocks
      verbatim in structure (Title / Depicts / Source page / Original file / Author / Date taken /
      Retrieved / Modifications / License + attribution string + test-only/not-in-APK note).
- [ ] Confirm all new fixtures live under `app/src/androidTest/` **only** (not in the production APK),
      and add a fixture-readability guard in the harness so a missing/corrupt/misnamed image fails
      loudly before inference.

## Phase 3 — Swap-evaluation harness

*Why: the prototype must be backed by comparative data, not by whichever model loads easiest. The
harness is the comparison substrate, and `verifyNoNetworking` runs the **moment** any candidate
dependency or acquisition script lands here — a breach caught now is a one-line revert, not days of
unwinding.*

- [ ] Introduce a test-only `ModelUnderTest` descriptor (model id, manifest asset path, labels asset,
      mapping asset, model asset path, input size/dtype) so the harness can drive AIY + candidate
      bundles without duplicating interpreter/preprocessor/mapper code. Reuse
      `TfLiteInterpreterFacade`, `ImagePreprocessor`, `ModelScoreMapper`, `ModelLabelMapReader`.
- [ ] Add candidate bundles under `app/src/main/assets/ml/<candidate_id>/` (`model.tflite`,
      `labels.csv`, `model_manifest.json`, `plant_class_map.json`, license file) **only** for a
      candidate that is bundle-ready and will be probed/wired. If a candidate is too large or its
      weights aren't redistributable, keep the conversion script + evidence under
      `docs/sprints/evidence/PLANTPOTTING-0007/`, mark it "not bundle-ready", and exclude it from the
      running prototype.
- [ ] **Run `verifyNoNetworking` immediately** after the first candidate dependency / asset /
      acquisition script lands — and re-run it after each subsequent one. Treat any new network
      surface as a stop-and-fix before continuing (do **not** wait for Phase 6).
- [ ] Build the harness as a dedicated GMD test (`ModelSwapEvaluationTest`) **alongside**, not
      replacing, `OnDeviceModelRealInterpreterTest` — keep the existing Monstera/jade/binding
      assertions intact as the regression anchor.
- [ ] Iterate the harness over `{baseline AIY} × {expanded fixtures}` and emit a machine-readable
      report at `docs/sprints/evidence/PLANTPOTTING-0007/model-swap-eval.csv` with columns: model id,
      fixture species id, expected species id, raw top-1 label, raw top-1 score, raw top-3
      labels+scores, mapped top-1 KB id, mapped top-3 KB ids, in-vocab flag, route (high/low-conf),
      source, **inference latency (ms)**, failure reason (if any).
- [ ] Iterate the harness over the **shortlisted candidate(s)** × the same fixtures into the same CSV,
      so AIY and candidates are directly comparable row-for-row. Feed each model its **own** native
      preprocessing from the canonical fixture.
- [ ] Capture **inference latency** per model/fixture (warm-run median over a few iterations) so an
      accurate-but-slow model is visible as a regression, not hidden — record it in the CSV and roll a
      per-model median into the summary.
- [ ] Add a regression assertion pinning the **AIY baseline rows** (Monstera high-conf @ ~**0.8984**,
      jade low-conf @ ~**0.1055**) so the harness can't silently mask a plumbing regression.
- [ ] Add **evidence-driven** candidate assertions *after* the probe (do not pre-commit a form):
      preferred = correct top-1 high-confidence for the required fixtures; acceptable documented
      fallback = correct species in top-3 with a recorded threshold/mapping reason.
- [ ] Write the human summary at
      `docs/sprints/evidence/PLANTPOTTING-0007/model-swap-eval-summary.md` comparing AIY vs candidates
      on top-1 count, top-3 count, in-vocab KB coverage, mean correct-class score, **median inference
      latency**, asset-size delta, and license status — and name the winner with rationale (or record
      "no public winner" with the fine-tuning-sprint recommendation). **Size and latency are
      selection-time criteria that can disqualify an otherwise-accurate candidate here, at the
      summary** — not just things measured later in Phase 6. A model that wins on top-1 but blows the
      bundle-size or latency budget is not the winner; record the trade-off explicitly.

## Phase 4 — Winning-model manifest & mapping (re-baseline)

*Why: mapping + thresholds are part of the swap, not later doc cleanup — and they must be settled
**before** wiring so the app path and harness share identical assets (no app/harness drift).*

- [ ] Author `app/src/main/assets/ml/<winning_model>/model_manifest.json`: source URL, variant,
      sha256, `placeholder=false`, input size, input dtype, color order, output tensor shape, label
      count, acquisition date, license, license file, labels asset, mapping asset, `thresholds`,
      `per_species_thresholds`, `_comment_coverage`. (Mirror the AIY manifest's comment discipline.)
- [ ] Author `app/src/main/assets/ml/<winning_model>/plant_class_map.json`: map every KB species
      present in the winner's vocabulary to the correct `kbSpeciesId`, including `alias: true` rows
      where the model uses older/synonym names — **re-derive aliases from the winner's actual labels;
      do not assume AIY's alias set carries over.**
- [ ] Recompute `_comment_coverage` from the winner's **actual** labels and state both mapping-entry
      overlap and KB-species overlap (e.g. "N of 18 entries, M of 16 KB species") — the headline
      coverage delta vs AIY's 2/16.
- [ ] Re-baseline `per_species_thresholds` from the expanded fixture scores: seed an override **only**
      where the correct mapped class sits near the global and an absolute override cleanly closes the
      gap; **never lower thresholds to bless ~10% predictions.** Record each decision (+ the deciding
      number) — including "stays empty by design" if so.
- [ ] Add a PLANTPOTTING-0007 section to `docs/kb/ml-mapping-notes.md`: winning vocabulary, alias
      decisions, per-fixture outcomes, coverage change from AIY, per-species threshold decisions, and
      the license report.
- [ ] Update the affected JVM contract tests to the winner's bundle (keeping AIY-bundle coverage green
      because AIY stays the default flag target): `ModelManifestTest`, `ModelAssetsPresenceTest`,
      `ModelLabelMappingValidationTest`, `PerSpeciesThresholdsContractTest`,
      `ModelScoreMapperPerSpeciesThresholdTest`, `ModelManifestDtypeContractTest`.

## Phase 5 — Running-prototype wiring (G4 — the headline)

*Why: G4 is the whole point — the swap must run in the real app, not just the harness. One switch,
not a scattering of conditionals, and the seam stays frozen by default.*

- [x] Add a single model-root selection point: a `BuildConfig` field (or top-level constant)
      `ACTIVE_MODEL_ROOT` defaulting to `ml/aiy_plants_v1`, flipped to `ml/<winning_model>` on the
      prototype build/branch. **One switch — no scattered conditionals, no second `PlantIdentifier`
      implementation.** → `buildConfigField` in `app/build.gradle.kts`; locked by
      `ActiveModelRootContractTest` (R8: default stays AIY).
- [x] Refactor `OnDeviceIdentifyProvidersModule` (`OnDeviceIdentifyModule.kt`) so
      `provideModelManifest`, `provideModelLabels`, `provideModelLabelMap`, and
      `provideInterpreterFacade` read from `ACTIVE_MODEL_ROOT` instead of hardcoding
      `ml/aiy_plants_v1/...`. (`ModelManifestReader` / `ModelLabelsReader` / `ModelLabelMapReader`
      already take asset paths — thread the root through.) → done via `@ActiveModelRoot` qualifier;
      `compileDebugKotlin`, the 4 focused JVM tests, `ktlintCheck`, `verifyNoNetworking` all GREEN.
- [x] Keep `PlantIdentifier.identify(jpeg): IdentificationResult` and `IdentificationResult`
      **unchanged**. Confine all tensor-shape/dtype/preprocessing differences inside the
      interpreter/preprocessor layer behind the seam. **Budget real effort here** — adapting an
      arbitrary model's output tensor (e.g. PlantNet's large label space) into the existing
      `IdentificationResult` without a seam break is the likeliest place to get stuck (see R7). If the
      winner genuinely can't fit the seam, **stop**, record the exact reason in the results doc, then
      update all callers/tests in this phase. → **Seam unchanged.** The candidate is MobileNetV2 →
      [1,47] output → already fits the existing `ModelScoreMapper`/`IdentificationResult` path (only a
      smaller label space than AIY's 2102); FLOAT32 input maps onto the existing `ImagePreprocessor`
      FLOAT32 branch. No seam change needed.
- [ ] Confirm `OnDevicePlantIdentifier` still returns `IdSource.ON_DEVICE_MODEL` for the winner **and**
      still routes weak/out-of-vocab predictions to the existing low-confidence flow (picker
      unchanged) — assert both explicitly, not implicitly.
- [ ] Add/extend an instrumentation test that drives the **app-wired winning model** through
      `OnDevicePlantIdentifier.identify(...)` on at least Monstera, jade, snake plant, pothos, ZZ, and
      peace lily fixtures (the production path, not the bare harness).
- [ ] Perform a **live smoke run** on `pixel6Api34` (or device) with the flag/branch enabled and
      capture evidence (logcat/screenshot) that the app identifies **≥3 houseplant fixtures directly**
      through the production identifier path. This is the "not a paper spike" gate.

## Phase 6 — Constraint, size & integration gates (cheap → expensive)

*Why: order gates cheap→expensive so the sprint fails fast — a broken contract test should never wait
behind a 20-minute integration transcript.*

- [ ] Focused JVM tests first: manifest parsing, label mapping, score mapping, per-species thresholds,
      and `VerifyNoNetworkingRegressionTest` — all green.
- [ ] `verifyNoNetworking` GREEN at close-out (it was already run continuously in Phase 3 as each
      candidate dep/script landed; this is the final confirmation).
- [ ] `scripts/check-stub-isolation.sh` GREEN — the production swap must not leak fake/test
      identifiers into app wiring.
- [ ] Measure **asset/APK size impact**: raw winner model size, compressed APK delta vs AIY, and any
      `noCompress`/`aaptOptions` implication; record in `model-swap-eval-summary.md`.
- [ ] Instrumented tests on `pixel6Api34`: `OnDeviceModelRealInterpreterTest` (AIY baseline rows still
      green), `ModelSwapEvaluationTest`, and the Phase 5 app-wired prototype test.
- [ ] Full `testDebugUnitTest` + `ktlintCheck` GREEN.
- [ ] `scripts/integration-flow.ps1` cold / warm / buildonly — record whether expected artifacts stay
      unchanged or why a re-baseline is intentionally required.
- [ ] Write `docs/sprints/results/PLANTPOTTING-0007.md`: selected model (or "no public winner +
      fine-tuning-sprint recommendation"), rejected candidates + why, fixture results table,
      top-1/top-3 + coverage delta vs AIY, **median latency comparison**, threshold changes, license
      report, size report, **live-prototype status (with evidence link)**, and gate results.
- [ ] **Flip the ledger entry to `done` only after every gate above is green** (or carries a
      documented, accepted blocker in the results doc).

## Sequencing & dependencies

```
Phase 1 (survey + matrix)  ──> shortlist (<=2 candidates, or "none viable")
        |                         |
        v                         v
Phase 2 (fixtures) ───────> Phase 3 (harness: baseline + candidates -> CSV(+latency) + summary -> WINNER)
   (parallel w/ candidate           |   [verifyNoNetworking runs continuously here, on each dep/script]
    conversion; fixtures            v
    are model-agnostic)     Phase 4 (winner manifest + map + thresholds + contract tests)
                                    |
                                    v
                            Phase 5 (ACTIVE_MODEL_ROOT switch + app-wired test + LIVE smoke run)
                                    |
                                    v
                            Phase 6 (gates cheap->expensive: JVM -> net-free/stub -> size -> instrumented
                                     -> full+ktlint -> integration-flow -> results doc -> ledger flip)
```

- **Phase 1 gates everything model-side** — no download/convert/check-in before license, weight
  availability, size, latency, and KB-overlap are known.
- **Phase 2 runs in parallel** with candidate conversion once the required species list is fixed;
  fixtures are reusable regardless of which candidate wins.
- **Phase 3 must complete before Phase 5** — the prototype is backed by comparative data, not by
  whichever model loads easiest. Net-free is a **continuous** gate inside Phase 3, not a Phase-6
  afterthought.
- **Phase 4 immediately follows winner selection** — mapping + thresholds are part of the swap.
- **Phase 5 only after the winner's manifest/map are stable** so the app path and harness share the
  same assets.
- **Phase 6 cheap → expensive**: focused JVM → net-free/stub gates → size → instrumented → full suite
  + ktlint → integration-flow → results doc → ledger flip.

## Risks & mitigations

- [ ] **R1 — Public PlantNet/iNat weights aren't redistributable even though datasets/APIs are
      public.** *Mitigation:* record license + weight-availability **separately** in the matrix. There
      is **no in-sprint training fallback** (see non-goals) — if no public model is redistributable
      and bundle-fit, the running prototype demonstrates the *swap mechanism* with the best probeable
      candidate (worst case AIY), and the deliverable becomes the negative finding + a recommendation
      to spin a dedicated fine-tuning sprint.
- [ ] **R2 — Acquisition/conversion fragility: a "nominally available" checkpoint isn't a
      bundle-ready TFLite model.** Even PlantNet-300K (not just PlantCLEF/ViT) may publish a *training*
      architecture that needs nontrivial conversion, hits unsupported TFLite ops, or won't compress to
      a bundle-plausible INT8. *Mitigation:* record **conversion effort + result** in the matrix, not
      just availability; require a clean conversion proof + bundle-plausible size *before* a candidate
      earns a fixture probe; prefer MobileNetV3-Small/EfficientNet-Lite for bundle fit; time-box
      conversion spelunking and fall back to the next candidate rather than sinking the sprint into it.
- [ ] **R3 — A model wins on accuracy but is too slow on-device.** *Mitigation:* capture inference
      latency as a first-class evaluation axis (Phase 3 CSV + summary); an accurate-but-slow model
      (multi-second inference) is recorded as a regression and weighed against the AIY baseline before
      selection.
- [ ] **R4 — Selection bias: choosing a winner on too few fixtures overfits the model *choice*.**
      *Mitigation:* the expanded ≥6-species fixture set (G2) is the ruler; never select a winner on
      Monstera + jade alone. (Promoted from GEMINI's draft — the sharpest articulation of this risk.)
- [ ] **R5 — Candidate labels use synonyms/common names that break direct mapping.** *Mitigation:*
      audit labels against `species.json` aliases; encode every alias in the winner's
      `plant_class_map.json` + document in `ml-mapping-notes.md`.
- [ ] **R6 — Lowered thresholds hide a weak model behind optimistic routing.** *Mitigation:* seed
      `per_species_thresholds` only from fixture evidence near the global; keep weak predictions
      low-confidence and report them honestly (the 0006 discipline).
- [ ] **R7 — TFLite I/O shape/dtype/large-label-space mismatch tempts a seam break.** *Mitigation:*
      confine pre/post-processing inside the interpreter/preprocessor layer; budget real effort for
      adapting an arbitrary output tensor into `IdentificationResult`; the seam changes only with a
      documented, caller-updating justification (default: it doesn't). *This is the likeliest place to
      get stuck — do not underestimate it.*
- [ ] **R8 — Wiring hardcodes a candidate or deletes AIY before comparison is done.** *Mitigation:*
      single `ACTIVE_MODEL_ROOT` switch defaulting to AIY; keep AIY baseline + manifest/coverage tests
      alive through close-out.
- [ ] **R9 — A model-acquisition/conversion script smuggles in a networking path.** *Mitigation:*
      acquisition scripts live outside production runtime, are never invoked from Gradle app tasks, and
      `verifyNoNetworking` is run **the moment** each script/dep lands (Phase 3), not only at close-out.
- [ ] **R10 — Model asset bloats the APK.** *Mitigation:* measure compressed + uncompressed deltas
      before picking the winner; prefer INT8; reject not-bundle-friendly models even if licensing is
      fine. Report the trade-off.
- [ ] **R11 — GMD/probe flakiness or environment drift.** *Mitigation:* reuse the green `pixel6Api34`
      GMD harness; re-run `integration-flow.ps1` cold/warm/buildonly before close-out.
- [ ] **R12 — Sprint degrades into a paper spike.** *Mitigation:* G4 + the Phase 5 live smoke run are
      **acceptance-blocking** — the results doc must link running-prototype evidence.

## Acceptance criteria

- [ ] `docs/sprints/evidence/PLANTPOTTING-0007/model-candidate-matrix.md` exists and reports
      availability, license, size, latency profile, conversion risk, and 16-species vocabulary overlap
      for AIY plus ≥2 serious replacement candidates (or documents that <2 cleared the bar, with the
      survey outcome recorded).
- [ ] `app/src/androidTest/assets/identify-fixtures/` contains Monstera + jade (byte-unchanged) plus
      ≥4 new canonical fixtures (snake plant, pothos, ZZ, peace lily) + 2 more KB species — all with
      complete CC provenance in `LICENSE.txt` and all test-only (not in the APK).
- [ ] The swap-evaluation harness runs AIY **and** the shortlisted candidate(s) over the expanded
      fixtures and writes `model-swap-eval.csv` + `model-swap-eval-summary.md` comparing top-1/top-3,
      in-vocab coverage, **inference latency**, size, and license against the AIY baseline.
- [ ] A winning replacement model is selected from evidence and has a complete bundle under
      `app/src/main/assets/ml/<winning_model>/` — **or** the sprint explicitly records that no public
      candidate cleared the bar, with the negative finding + a fine-tuning-sprint recommendation in the
      results doc (no in-sprint training attempted).
- [ ] The winning model (or, in the no-winner case, the swap mechanism with the best probeable model)
      is wired behind a flag/branch via a single `ACTIVE_MODEL_ROOT` switch, and a **live
      device/emulator run** identifies ≥3 real houseplant fixtures directly through
      `OnDevicePlantIdentifier` (`source == ON_DEVICE_MODEL`) without network access — evidence linked
      from the results doc.
- [ ] `PlantIdentifier` / `IdentificationResult` remain unchanged — or the results doc carries a
      strong justification for the seam change and all callers/tests are updated in the same sprint.
- [ ] The winning model's `plant_class_map.json` (`_comment_coverage`) is re-baselined from its actual
      vocabulary, and `model_manifest.json` (`per_species_thresholds`) is **re-evaluated** from the
      fixture probes — seeded **only** where the evidence clearly warrants it and explicitly left
      "empty by design" otherwise. (6–8 fixtures is thin evidence for per-class thresholds; do not let
      "re-baselined" overstate what the data supports — the 0006 discipline holds.) Every threshold
      decision (and deciding number) is recorded.
- [ ] `docs/kb/ml-mapping-notes.md` has a PLANTPOTTING-0007 section: coverage delta, fixture outcomes,
      alias decisions, threshold decisions, latency, and license notes.
- [ ] `verifyNoNetworking` (run continuously through Phase 3 **and** at close-out),
      `scripts/check-stub-isolation.sh`, focused model/manifest/score JVM tests, GMD real-interpreter +
      swap-eval + app-wired prototype tests, full `testDebugUnitTest`, `ktlintCheck`, and
      `scripts/integration-flow.ps1` cold/warm/buildonly are GREEN — or carry a documented, accepted
      blocker in `docs/sprints/results/PLANTPOTTING-0007.md`.
- [ ] Asset/APK size impact of the winning model is measured and recorded (raw + compressed delta).
- [ ] The sprint does **not** end as a paper spike: the results doc links a running app-prototype state
      where the selected model identifies plants live behind the agreed flag/branch.
