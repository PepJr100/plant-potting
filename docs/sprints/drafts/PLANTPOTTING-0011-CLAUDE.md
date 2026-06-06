# PLANTPOTTING-0011 — Accuracy & trust sprint (measure → abstain → try-to-improve)

> Plan draft (CLAUDE). Refined intent: `docs/sprints/drafts/_CONTEXT-0011.md`. Every actionable item
> below is a `- [ ]` checkbox. This sprint diverges deliberately from the old roadmap "Next"
> (pothos↔Pilea), which **stays deferred** — the 0010 on-device review re-pointed us at the headline
> gap instead.

---

## Intent (restatement)

The production classifier (`house_plant_species_mobilenetv2`, FLOAT32 I/O, 47 classes) is **confidently
wrong** on real-world captures: the principal reports a real snake plant is correctly identified only
~1-in-3, and the other ~2/3 it confidently names a *different* plant. This is **not a 0010 regression**
(model + labels + thresholds were untouched) — it is an uncalibrated model meeting messy phone photos
now that the app shows a confident %+bar. This sprint confronts that **behind the frozen
`PlantIdentifier` seam, network-free, with no model swap and no training**, in three moves:
**(1) MEASURE** — get an honest, documented top-1 accuracy and *confident-wrong rate* from an expanded
CC0/PD fixture set plus synthetic perturbations; **(2) ABSTAIN** — tune the existing
`ModelScoreMapper` + `ModelManifest.Thresholds` gating (and add a margin-based abstention *above* the
plain gate) so confident-wrong predictions route to the "pick manually" picker instead of a
confident-wrong card; **(3) TRY TO IMPROVE** raw accuracy with no-training, behind-the-seam
preprocessing levers (center-crop vs full-frame squash, multi-crop / TTA, orientation handling) —
**adopt only what the harness numbers measurably support.** Plus two small 0010-review fold-ins.

## Goals

- A **reproducible eval harness** that prints a single honest scorecard — **top-1 accuracy**,
  **top-3 accuracy**, and **confident-wrong rate** (predictions that clear the high-confidence gate but
  name the wrong species) — over (a) clean CC0/PD real photos and (b) synthetic perturbations of those
  photos, for the production model. A real **before** number, checked into evidence.
- A **measured before/after for abstention**: confident-wrong rate **down**, with the expected,
  accepted cost (more low-confidence routing) quantified — not asserted by vibes.
- A **measured verdict on preprocessing changes**: each lever (center-crop, multi-crop/TTA, orientation)
  kept only if it improves top-1 on the eval set, dropped otherwise; the decision + the latency cost
  recorded in evidence.
- Two fold-ins from the 0010 review: thicker `ResultScreen` confidence bar; botanical-plate reference
  images replaced with CC0/PD photographs where a clean one exists.
- Hard seams stay frozen and all guards stay GREEN throughout (`verifyNoNetworking`,
  `check-stub-isolation.sh`, `HousePlantClassMapValidationTest` Pilea-absence guard, AIY baseline anchor).

## Non-goals (hold the line — do not plan work that violates these)

- [ ] **No model swap, no training, no fine-tune.** Same bundled `house_plant_species_mobilenetv2`
      `model.tflite`; AIY stays the pinned regression baseline. Only the FLOAT32 production model is the
      accuracy subject.
- [ ] **No self-shot / first-party imagery anywhere — including eval fixtures.** Every real photo added
      is **CC0 / public-domain**, license-verified (Wikimedia Commons), with a checked-in attribution
      block (extend `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`) and a cross-check test.
      Synthetic *perturbations* are derived in-process from those clean photos — not new imagery.
- [ ] **`PlantIdentifier` / `IdentificationResult` / `IdSource` stay byte-for-byte frozen** (0003 §4.4).
      All new behaviour rides existing seams (`OnDevicePlantIdentifier`, `ImagePreprocessor`,
      `ModelScoreMapper`, `ModelManifest`, `CandidateProvider`) and side-channels.
- [ ] **Do NOT map Pilea**; the CI Pilea-absence guard in `HousePlantClassMapValidationTest` stays
      GREEN. No pothos/Pilea special-casing (general abstention helping it incidentally is fine, not a
      target).
- [ ] **No AGP / Kotlin / Compose / Hilt / TFLite version bumps.**
- [ ] **`verifyNoNetworking` and `scripts/check-stub-isolation.sh` stay GREEN** at every commit.
- [ ] **No per-species threshold seeded to "bless" a weak prediction** (0006 anti-overfit discipline) —
      `per_species_thresholds` may be seeded only under the explicit evidence gate in Phase 2.
- [ ] **Deferred, do not plan:** My Plants surviving uninstall (backup/export); pothos↔Pilea / Pilea KB
      entry; any new model / fine-tune / training-data work.

---

## Phased task list

### Phase 0 — Scaffolding & evidence dir (no behaviour change)

- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0011/` with a `README.md` stub describing the artifacts
      this sprint will drop here (scorecard CSV/MD, abstention before/after, preprocessing decision log)
      and the `adb pull` recipe (mirror the `ModelSwapEvaluationTest` header convention).
- [ ] Confirm the local bar up front: `./gradlew testDebugUnitTest` green, `compileDebugAndroidTestKotlin`
      clean (instrumented sources compile), `verifyNoNetworking` + `scripts/check-stub-isolation.sh`
      green. Record the pre-sprint state in the evidence README so every later "GREEN throughout" claim
      has a baseline. (GMD `pixel6Api34` is CI-only — note that explicitly.)

### Phase 1 — MEASURE (the honest before-number)

**1a. Expand the clean real-photo fixture set (CC0/PD only).**
- [ ] Identify the highest-value mapped species to add as fixtures beyond today's 8
      (`crassula-ovata, dracaena-trifasciata, epipremnum-aureum, goeppertia-orbifolia,
      monstera-deliciosa, phalaenopsis, spathiphyllum-wallisii, zamioculcas-zamiifolia`). Prioritise
      **popular, in-vocab, mapped** species the principal is likely to photograph (e.g. `ficus-elastica`,
      `aloe-vera`, `chlorophytum-comosum`, `hedera-helix`, `euphorbia-pulcherrima`, `aglaonema`,
      `anthurium-andraeanum`, `dieffenbachia`, `kalanchoe`, `dypsis-lutescens`/`chamaedorea-elegans`,
      `maranta-leuconeura`/`ctenanthe`). Target **adding 8–16** new clean fixtures (accept fewer if the
      license-clean supply is thin — 0008 found only ~1–6.8% of houseplant imagery is license-clean;
      **log every species that could not be sourced cleanly** rather than silently skipping).
- [ ] Source each via `scripts/source-reference-images.ps1` / `source-missing-images.ps1` (Wikimedia
      Commons, `LicenseShortName ∈ {CC0, Public domain, PD, No restrictions}` — note CC-BY-SA is allowed
      for **test-only** fixtures, as the existing fixtures already are, but prefer CC0/PD). Center-crop
      to square, scale to **480×480**, re-encode **JPEG q80** — matching the existing fixture
      dimensions/encoding exactly. Name each `<kb-species-id>.jpg`.
- [ ] For every new fixture, append a full provenance block (Title / Source page / Original file /
      Author / Date / Original size / License + attribution) to
      `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`, under a new
      `PLANTPOTTING-0011` banner.
- [ ] Add a JVM-or-instrumented **fixture-license cross-check** guard (extend the existing
      `fixtureReadabilityGuardFailsLoudlyOnMissingOrCorruptImages` in `ModelSwapEvaluationTest`, or a new
      `IdentifyFixtureLicenseManifestTest`): assert every `identify-fixtures/*.jpg` has a matching named
      heading in `LICENSE.txt` (mirror `ReferenceImageManifestTest`). Fails CI if a fixture lacks
      attribution.

**1b. Synthetic perturbation generator (network-free, in-process).**
- [ ] Add a deterministic perturbation helper under `app/src/androidTest/.../identify/` (e.g.
      `FixturePerturbations.kt`) that takes a decoded `Bitmap` and emits a fixed, documented family:
      **blur** (radius set), **center-crop / off-center-crop** (zoom factor), **rotate** (±15°, ±90°),
      **brightness/contrast** shift (±). All Android-graphics-only (`Canvas`/`Matrix`/`ColorMatrix` or a
      simple box-blur) — **no new dependency, no network.** Use **fixed parameters** (no RNG; or a seeded
      generator) so runs are reproducible (note: `Math.random` is banned in workflow scripts but fine in
      test code — still prefer fixed params for determinism).
- [ ] Unit-style guard that the generator is deterministic and label-preserving in intent (it perturbs
      pixels, never changes the expected `<kb-species-id>` — the expected label is the source fixture's).

**1c. Scorecard harness (extend the 0006/0007-style eval).**
- [ ] Add a new instrumented test (e.g. `AccuracyEvalTest` next to `ModelSwapEvaluationTest`, reusing its
      `probeModel` plumbing) that, **for the production model only**, runs each clean fixture *and* each
      perturbation through the production preprocessor → facade → `ModelScoreMapper`, and records per-row:
      expected id, raw top-1 label+score, mapped top-1, top-3, route (`high-conf`/`low-conf`),
      `confident_wrong` (route == high-conf AND mapped top-1 ≠ expected), perturbation kind, latency.
- [ ] Emit machine-readable artifacts to the external files dir + logcat (reuse the CSV writer pattern):
      `accuracy-eval.csv` (per-row) and `accuracy-eval-summary.md` (aggregate). The summary must print, as
      a single scorecard, for **clean** and for **each perturbation family** and **overall**:
      **top-1 accuracy**, **top-3 accuracy**, **confident-wrong rate**, **abstain (low-conf) rate**,
      **median latency**.
- [ ] Pin the **AIY baseline anchor** unchanged (Monstera 0.8984 high-conf, jade low-conf) so the harness
      can't silently drift the plumbing (reuse the existing assertions; do not weaken them).
- [ ] Run on GMD `pixel6Api34` in CI; pull `accuracy-eval.csv` + `-summary.md` and **commit them to
      `docs/sprints/evidence/PLANTPOTTING-0011/`** as the documented BEFORE number (thresholds + model
      untouched at this point).

### Phase 2 — ABSTAIN (route confident-wrong → manual picker; measurable before/after)

**2a. Add a margin-based abstention lever above the plain gate (the core new capability).**
- [ ] Extend `ModelManifest.Thresholds` with **one new optional field**, e.g.
      `highConfidenceAbstainMargin` (JSON `high_confidence_abstain_margin`), **defaulting to `0f`
      (disabled)** so the AIY manifest and baseline behaviour are **byte-for-byte unchanged**. Parse it in
      `ModelManifestReader.parse` with `?: 0f`. Update `ModelManifestDtypeContractTest` /
      `ModelManifestTest` as needed for the new optional field.
- [ ] In `ModelScoreMapper.map`, after computing `highConfDirect || highConfMargin`, add an
      **abstention veto**: if the verdict would be high-confidence-direct but `(bestProb - secondProb) <
      thresholds.highConfidenceAbstainMargin`, **downgrade to low-confidence** (route to the picker with
      the same mapped candidates). When the field is `0f` this is a no-op → AIY unchanged. Keep the
      existing margin-*branch* logic intact; this is an *additional* gate, not a replacement.
- [ ] JVM unit tests in `ModelScoreMapperTest` for the new gate using **synthetic score vectors** (no
      device needed): (i) high prob + tiny top1-top2 margin → abstains; (ii) high prob + wide margin →
      stays high-conf; (iii) margin = 0 default → behaviour identical to today (regression). This makes
      abstention logic **measurable on the JVM** independent of the GMD fixture set.

**2b. Tune the gate against the Phase-1 scorecard (evidence-driven, anti-overfit).**
- [ ] Using `accuracy-eval.csv`, sweep candidate settings of the levers the context names — raise the
      global `high_confidence_plain` (currently 0.55), tighten/relax the margin branch
      (`high_confidence_margin_min` 0.45 / `high_confidence_margin_delta` 0.18), and set
      `high_confidence_abstain_margin` — and tabulate confident-wrong rate vs abstain rate at each
      setting. Choose the setting that **minimises confident-wrong** within an explicitly stated abstain-
      rate budget (state the budget; e.g. "accept up to X% more low-conf routing").
- [ ] **Anti-overfit guard (central methodological risk):** evaluate the chosen setting under
      **leave-one-species-out** / clean-vs-perturbation split — report confident-wrong on the
      *perturbation* rows (which the threshold was *not* hand-tuned against) as the honest generalisation
      number, not just the clean rows. Document this split in the summary.
- [ ] Apply the chosen **global** threshold change to
      `app/src/main/assets/ml/house_plant_species_mobilenetv2/model_manifest.json` (and update its
      `_comment_thresholds`). **Only** seed a `per_species_thresholds` entry if a single in-vocab species
      is a chronic confident-wrong offender AND a per-class value cleanly fixes it **without** harming
      others on the eval set — otherwise leave `per_species_thresholds: {}` (document the decision either
      way; respect the existing `PerSpeciesThresholdsContractTest` / `ModelScoreMapperPerSpeciesThresholdTest`).
- [ ] Re-run the GMD scorecard and commit the **AFTER** `accuracy-eval-summary.md`; the abstention
      before/after table (confident-wrong ↓, abstain ↑) is the headline deliverable of this phase.

### Phase 3 — TRY TO IMPROVE raw accuracy (preprocessing levers, decision-driven)

> All of this is behind the seam in `ImagePreprocessor` / `OnDevicePlantIdentifier`. **Keep only what the
> harness shows measurably improves top-1; drop the rest.** Gate every adopted change behind a manifest
> flag (default off) so AIY and the current production path are untouched until evidence says otherwise.

- [ ] **Center-crop vs full-frame squash.** Today `ImagePreprocessor` does a non-aspect-preserving
      `ResizeOp(inputSize, inputSize, BILINEAR)` — a squash. Add a **center-crop-then-resize** option
      (crop to the centered square first, then resize) controlled by a new optional manifest field (e.g.
      `preprocess_mode: "squash" | "center_crop"`, default `"squash"` so nothing changes until adopted).
      The MobileNetV2 TF-Hub convention typically expects center-crop; measure it.
- [ ] **Multi-crop / TTA.** Add an optional `ImagePreprocessor.preprocessVariants(jpeg): List<PreprocessedImage>`
      (e.g. center + 4 corner crops, optional horizontal flip) and have `OnDevicePlantIdentifier` run
      inference over each and **average the softmax score vectors** before `mapper.map(...)`. Gate behind
      a manifest `tta` count (default `1` = single crop = current behaviour). Measure top-1 delta **and
      the per-identify latency cost** on GMD (the single-shot path is already the bar; TTA multiplies
      interpreter runs).
- [ ] **Orientation / aspect handling.** Verify EXIF/orientation is handled before crop (phone captures
      are often rotated); add rotation normalisation only if the perturbation rows (`rotate ±90°`) show it
      helps. (No camera-path contract change — operate on the decoded bitmap inside the preprocessor.)
- [ ] **Decision log.** For each lever, record in `docs/sprints/evidence/PLANTPOTTING-0011/preprocessing-decision.md`:
      top-1 before/after, confident-wrong before/after, median latency before/after, and **ADOPT / DROP**
      with the number that drove it. Flip the manifest default **only** for adopted levers; leave the
      others wired-but-off (so a future sprint can revisit without re-plumbing).
- [ ] If a lever is adopted, re-run Phase-1/Phase-2 scorecards on top of it and re-commit the final
      AFTER summary so the published number reflects the shipped preprocessing.

### Phase 4 — 0010-review fold-ins (small, independent)

- [ ] **Thicken the confidence bar.** In `ResultScreen.kt` (the `LinearProgressIndicator` at ~line 123),
      bump the bar height (e.g. via `Modifier.height(...)`), keeping the existing `progress = { pct/100f }`
      and the `ResultScreenTags.CONFIDENCE_PCT`/bar test tags intact. Adjust/confirm any
      `ResultScreen` Compose test that asserts the bar.
- [ ] **Replace botanical-plate reference images with photographs where a clean one exists.** Target the
      plates called out in the 0010 handoff: peace lily (`spathiphyllum_wallisii`), poinsettia
      (`euphorbia_pulcherrima`), parlor palm (`chamaedorea_elegans`), dracaena (`dracaena`),
      philodendron-pink-princess (`philodendron_pink_princess`). For each, re-run
      `source-reference-images.ps1` for a CC0/PD **photograph**; if one exists, replace the `.webp` under
      `res/drawable-nodpi/` (≤480px WebP ~q60, same `PlantImageResolver` key). **If no clean photo
      exists, leave the plate and document why** in the manifest.
- [ ] Update `docs/licenses/reference-images.md` (rows for any swapped image) and ensure
      `ReferenceImageManifestTest.everyBundledReferenceImageHasAManifestEntry` stays GREEN.

### Phase 5 — Documentation, evidence & close

- [ ] Update `docs/kb/ml-mapping-notes.md` with the calibration story: the BEFORE confident-wrong number,
      the abstention setting chosen + why, the preprocessing ADOPT/DROP decisions, and the AFTER number.
- [ ] Ensure all evidence artifacts are committed under `docs/sprints/evidence/PLANTPOTTING-0011/`
      (scorecard CSV + before/after summaries, abstention table, preprocessing decision log).
- [ ] Final guard sweep: `testDebugUnitTest`, instrumented sources compile-clean,
      `verifyNoNetworking`, `check-stub-isolation.sh`, `HousePlantClassMapValidationTest` (Pilea absent),
      AIY baseline anchor, reference + fixture license cross-checks — **all GREEN**. Record in the
      evidence README.
- [ ] (Optional, if value seen per 0010 coverage note) a small instrumented check that the "Add this
      plant" path triggers on a confident-but-unmapped class — **only** if it falls out cheaply; not a
      target.

---

## Sequencing & dependencies

1. **Phase 0** first (evidence dir + baseline green) — cheap, unblocks honest before/after claims.
2. **Phase 1 must precede Phases 2 & 3** — you cannot tune abstention or judge preprocessing without the
   scorecard. Within Phase 1: 1a (fixtures) and 1b (perturbations) can proceed in parallel; 1c (harness)
   depends on both, and its GMD run produces the BEFORE number.
3. **Phase 2 (abstain) and Phase 3 (improve) are largely independent** but interact: if Phase 3 adopts a
   preprocessing change, **re-run Phase 2's tuning on top of it** (thresholds calibrated to the *shipped*
   pipeline, not a discarded one). Recommended order: do Phase 2 abstention first (pure gating, JVM-
   testable, low risk), then Phase 3, then a final Phase-2 re-tune + re-run if Phase 3 adopted anything.
4. **Phase 4 fold-ins are fully independent** of 1–3 (UI + assets) and can be done any time / in parallel.
5. **Phase 5** closes after 1–4 land.

Hard ordering rule: **every manifest/threshold/preprocessing change must default to current behaviour**
(new fields default to disabled) so each commit keeps AIY + the existing tests green; flip defaults only
once evidence in this sprint justifies it.

## Risks & mitigations

- **★ Overfitting thresholds/preprocessing to a tiny clean fixture set (central risk).** The clean set is
  small (license scarcity) and not representative of messy captures.
  *Mitigations:* (a) judge generalisation on the **perturbation rows the gate was not tuned against** and
  on a **leave-one-species-out** split, reporting those as the honest numbers; (b) prefer **global**
  threshold + preprocessing changes over per-species seeds; gate any per-species seed behind the existing
  0006 evidence discipline; (c) report **confident-wrong rate** (the thing we care about) not just top-1,
  so we don't trade real errors for a flattering accuracy headline.
- **License-clean photo scarcity (0008: ~1–6.8% clean).** May not reach 8–16 new fixtures.
  *Mitigation:* accept a smaller real set, **lean on synthetic perturbations** for robustness coverage,
  and **log every species that couldn't be sourced** (no silent truncation) so the eval's limits are
  explicit.
- **Synthetic perturbations ≠ real-world messiness.** Blur/rotate/brightness approximate but don't equal
  phone-capture failure modes. *Mitigation:* label perturbation results as *synthetic-robustness*, keep
  clean-photo numbers reported separately, and frame the abstention win as "confident-wrong ↓ under
  stress," not "solves real-world accuracy."
- **TTA latency vs benefit.** Averaging N crops multiplies interpreter runs. *Mitigation:* the harness
  records per-identify latency; adopt TTA only if top-1 gain justifies the measured cost, and keep N
  small (≤5); default off if marginal.
- **GMD CI flakiness / transient timeouts** (known: GMD step can 1h-timeout then pass on rerun; see the
  Settings-pollution lesson). *Mitigation:* keep new instrumented tests self-contained (no real-app
  intents), pull the `gradle-reports` JUnit XML for diagnosis, rerun the GMD job rather than weakening
  assertions.
- **Accidentally changing AIY baseline or the frozen seam.** *Mitigation:* all new manifest fields
  optional + default-disabled; AIY anchor assertions kept verbatim; `PlantIdentifier`/`IdentificationResult`
  untouched; rely on existing contract tests (`PlantIdentifierContractTest`, `CandidateBundleContractTest`,
  `ActiveModelRootContractTest`) staying green.
- **Manifest schema drift breaking the parser.** New JSON keys must be tolerated by both manifests.
  *Mitigation:* `Json { ignoreUnknownKeys = true }` already set; parse new fields with safe defaults; add a
  manifest test for the new optional fields.

## Acceptance criteria

- [ ] An expanded CC0/PD fixture set (target +8–16, actual count + any un-sourceable species documented),
      every fixture license-attributed in `identify-fixtures/LICENSE.txt` and enforced by a cross-check
      test.
- [ ] A deterministic, network-free synthetic-perturbation generator (blur/crop/rotate/brightness) with a
      determinism guard.
- [ ] A committed **BEFORE scorecard** (`accuracy-eval.csv` + `accuracy-eval-summary.md`) reporting top-1,
      top-3, **confident-wrong rate**, abstain rate, and median latency — separately for clean and each
      perturbation family — for the production model, with the AIY baseline anchor intact.
- [ ] A `high_confidence_abstain_margin` lever in `ModelManifest.Thresholds` + `ModelScoreMapper`,
      default-disabled (AIY/baseline unchanged), with JVM unit tests proving the abstain/keep/no-op cases.
- [ ] A committed **AFTER scorecard** showing **confident-wrong rate down** vs the BEFORE, with the
      accepted abstain-rate cost stated, and the chosen settings applied to the production
      `model_manifest.json` — validated on the perturbation/leave-one-out split to show it isn't overfit.
- [ ] A `preprocessing-decision.md` recording ADOPT/DROP for center-crop, TTA, and orientation, each with
      the top-1 / confident-wrong / latency numbers that drove the call; only adopted levers have their
      manifest default flipped.
- [ ] Confidence bar is visibly thicker on `ResultScreen` (test still green); called-out botanical plates
      replaced with CC0/PD photos where one exists (reference-image manifest + `ReferenceImageManifestTest`
      green), with any remaining plate's reason documented.
- [ ] `PlantIdentifier`/`IdentificationResult`/`IdSource` unchanged; Pilea still unmapped
      (`HousePlantClassMapValidationTest` green); `verifyNoNetworking` + `check-stub-isolation.sh` green;
      JVM unit tests green and instrumented sources compile-clean; AIY baseline anchor unchanged.
- [ ] `docs/kb/ml-mapping-notes.md` updated with the before→after calibration narrative; all evidence
      artifacts committed under `docs/sprints/evidence/PLANTPOTTING-0011/`.
