# PLANTPOTTING-0011 — Accuracy & trust sprint (measure → improve → abstain)

> Merged plan (Opus synthesis of the CODEX / GEMINI / CLAUDE drafts + the CODEX and CLAUDE
> cross-critiques; the GEMINI critique could not be produced — `agy` did not write the file across two
> attempts — so the merge draws on two critiques plus all three drafts). Drafts and critiques live under
> `docs/sprints/drafts/PLANTPOTTING-0011-*.md`; refined intent at
> `docs/sprints/drafts/_CONTEXT-0011.md`. Every actionable item below is a `- [ ]` checkbox.

## Intent

The 0010 on-device review surfaced the project's headline gap: the production classifier
(`house_plant_species_mobilenetv2`, FLOAT32 I/O, 47 classes) is **confidently wrong** on real-world
captures — the principal reports a real snake plant is correctly identified only ~1-in-3, and the other
~2/3 it confidently names a *different* plant. This is **not a 0010 regression** (model + labels +
thresholds were untouched); it is an uncalibrated model meeting messy phone photos now that the app shows
a confident %+bar. This sprint confronts it **behind the frozen `PlantIdentifier` seam, network-free,
with no model swap and no training**, in three moves, sequenced **MEASURE → IMPROVE → ABSTAIN** so
confidence thresholds are calibrated **once, against the pipeline that actually ships**:
**(1) MEASURE** — an honest, documented scorecard (top-1, top-3, **confident-wrong rate**, abstain rate,
latency) over an expanded **CC0/PD-only** real-photo fixture set plus deterministic synthetic
perturbations; **(2) IMPROVE** — no-training, behind-the-seam preprocessing levers (center-crop vs
full-frame squash, multi-crop/TTA, orientation) adopted only where the harness numbers measurably support
it, locking the shipping pipeline; **(3) ABSTAIN** — tune the existing `ModelScoreMapper` /
`ModelManifest.Thresholds` gating (plus a new default-disabled margin-based abstention *above* the plain
gate) so confident-wrong predictions route to the "pick manually" picker instead of a confident-wrong
card, with a measured before/after validated on held-out rows. Plus two small 0010-review fold-ins and a
version-bumped debug APK for on-device feel.

**Honesty framing (load-bearing — repeated in risks + acceptance):** the eval set is necessarily small
and CC0/PD-clean (license scarcity; no self-shot allowed), so it under-represents the principal's messy
real-world captures. The shipped deliverable is **"confident-wrong rate driven down under measurable
conditions, with raw accuracy improved where a no-training lever helps"** — explicitly **not** "real-world
accuracy solved." Synthetic perturbations are reported as *synthetic-robustness*, never as independent
real-world samples.

## Goals

- [ ] A **reproducible, GMD-run eval harness** that prints a single honest scorecard — **top-1 accuracy**,
      **top-3 accuracy**, **confident-wrong rate** (clears the high-confidence gate but names the wrong
      species), **abstain (low-conf) rate**, **median + worst latency** — reported separately for **clean**
      photos and **each perturbation family**, with **in-vocab-only** metrics broken out, and a committed
      **BEFORE** number in evidence.
- [ ] A **measured verdict on preprocessing** (center-crop, multi-crop/TTA, orientation): each lever kept
      only if it improves top-1 on the eval set, dropped otherwise; decision + latency cost recorded; the
      shipping pipeline locked before threshold tuning.
- [ ] A **measured before/after for abstention**: confident-wrong rate **down**, with the accepted cost
      (more low-confidence routing) quantified, and the chosen setting validated on **held-out** rows
      (leave-one-species-out + perturbation rows not tuned against) so it isn't overfit to a tiny clean set.
- [ ] Two 0010-review fold-ins: thicker `ResultScreen` confidence bar; botanical-plate reference images
      replaced with CC0/PD photographs where a clean one exists.
- [ ] Version bumped (`versionCode 4→5`, `versionName 0.4.0→0.5.0`) and a debug APK delivered for on-device
      verification of the abstention behaviour + UI changes.
- [ ] Hard seams frozen and all guards GREEN throughout (`verifyNoNetworking`, `check-stub-isolation.sh`,
      `HousePlantClassMapValidationTest` Pilea-absence, AIY baseline anchor).

## Non-goals (hold the line)

- **No model swap, no training, no fine-tune.** Same bundled `house_plant_species_mobilenetv2`
  `model.tflite`; AIY stays the pinned regression baseline. Only the FLOAT32 production model is the subject.
- **No self-shot / first-party imagery anywhere — including eval fixtures.** Every real photo added is
  **CC0 / public-domain only** (Wikimedia Commons etc.), license-verified, with a checked-in attribution
  manifest + a cross-check test. **CC-BY / CC-BY-SA are not accepted for new fixtures this sprint** (both
  critiques flagged any loosening as off-constraint). Synthetic *perturbations* are derived in-process from
  those clean photos — not new imagery. The principal will not supply photos, even uncommitted.
- **`PlantIdentifier` / `IdentificationResult` / `IdSource` stay byte-for-byte frozen** (0003 §4.4). All new
  behaviour rides existing seams (`OnDevicePlantIdentifier`, `ImagePreprocessor`, `ModelScoreMapper`,
  `ModelManifest`, `CandidateProvider`) and side-channels.
- **Do NOT map Pilea**; the CI Pilea-absence guard in `HousePlantClassMapValidationTest` stays GREEN. No
  pothos/Pilea special-casing (general abstention helping it incidentally is fine, not a target). The
  pothos↔Pilea boundary fix + Pilea KB entry stay **deferred**.
- **No AGP / Kotlin / Compose / Hilt / TFLite version bumps.**
- **No My Plants backup/export/uninstall-persistence work** (deferred per principal).
- **No per-species threshold seeded to "bless" a weak prediction** (0006 anti-overfit discipline);
  `per_species_thresholds` may be seeded only under the explicit evidence gate in Phase 3.

## Key decisions (resolve first — the rest depends on these)

### D1 — Sequencing: MEASURE → IMPROVE → ABSTAIN (tune thresholds once)
All three drafts sequenced measure→abstain→improve, which forces a threshold **re-tune** after any adopted
preprocessing change (preprocessing shifts the score distribution the thresholds were tuned against). The
CLAUDE critique's sharper order is adopted: **lock the raw pipeline first (preprocessing decided on
threshold-independent top-1), then tune abstention once against the pipeline that ships.** The BEFORE
scorecard is still captured at current thresholds + current preprocessing so the headline before/after is
honest.

### D2 — Honest measurement under the no-self-shot ban (the central methodological risk)
The clean CC0/PD set is small and not representative of messy captures; tuning thresholds against it
overfits. Defences, all required:
- **Multiple base photos per species** where license-clean supply allows — `<kb-species-id>__NN.jpg`
  naming (CODEX). More independent base images is the only honest way to grow effective sample count under
  the ban; perturbations of one photo are **not** independent samples.
- **Per-base-image-averaged** metrics alongside "all variants" (CODEX) so one heavily-perturbed photo can't
  dominate the headline.
- **Held-out generalisation number** (CLAUDE): tune on a subset, then report confident-wrong on the
  **perturbation rows the gate was not tuned against** plus a **leave-one-species-out** split — that is the
  honest number, distinct from the rows tuned against.
- **In-vocab-only metrics** broken out (CODEX): with 38/47 mapped, separate the in-vocab denominator from
  unmapped classes.
- Report **confident-wrong rate** as the primary metric (not just top-1) so we don't trade real errors for
  a flattering accuracy headline. Perturbation results labelled *synthetic-robustness*, never real-world.

### D3 — Backward-compatible, default-disabled levers (keep AIY + baseline byte-for-byte)
Every new manifest field defaults to current behaviour so each commit keeps AIY and the existing tests
green; defaults flip only once this sprint's evidence justifies it:
- `high_confidence_abstain_margin` (new, `ModelManifest.Thresholds`) → default **`0f`** (disabled; no-op).
- `preprocess_mode` → default **`"squash"`** (current non-aspect-preserving resize).
- `tta` crop count → default **`1`** (single crop = current behaviour).
The **AIY baseline anchor** (`OnDeviceModelRealInterpreterTest`: Monstera 0.8984 high-conf / jade
low-conf) is kept verbatim so a refactor of `ModelScoreMapper.map` or `ImagePreprocessor` can't silently
drift the score path.

## Task list (by phase)

### Phase 0 — Scaffolding & baseline green (no behaviour change)
- [x] Create `docs/sprints/evidence/PLANTPOTTING-0011/` with a `README.md` stub listing the artifacts this
      sprint drops here (BEFORE/AFTER scorecard CSV+MD, preprocessing decision log, abstention before/after)
      and the `adb pull` recipe (mirror the `ModelSwapEvaluationTest` external-files convention).
- [x] Record the pre-sprint green state in the evidence README: `./gradlew :app:testDebugUnitTest` green,
      `:app:compileDebugAndroidTestKotlin` clean, `verifyNoNetworking` + `bash scripts/check-stub-isolation.sh`
      green. (GMD `pixel6Api34` is CI-only — no local emulator; note explicitly.) Every later "GREEN
      throughout" claim anchors here.

### Phase 1 — MEASURE (the honest before-number)
**1a. Expand the clean real-photo fixture set (CC0/PD ONLY).**
- [x] Define the fixture target list from **mapped, popular, in-vocab** species the principal is likely to
      photograph, beyond today's 8 (`crassula-ovata, dracaena-trifasciata, epipremnum-aureum,
      goeppertia-orbifolia, monstera-deliciosa, phalaenopsis, spathiphyllum-wallisii,
      zamioculcas-zamiifolia`). Prioritise the failure the review reported (snake plant
      `dracaena-trifasciata`) and pothos (`epipremnum-aureum`), then candidates like `ficus-elastica`,
      `aloe-vera`, `chlorophytum-comosum`, `hedera-helix`, `euphorbia-pulcherrima`, `aglaonema`,
      `anthurium-andraeanum`, `dieffenbachia`. **Target +8–16 fixtures and ≥2 independent base photos for
      the priority species where clean supply allows.** (Target list encoded in `scripts/source-identify-fixtures.ps1`.)
- [x] Add only **CC0/public-domain** real-photo JPEG fixtures under
      `app/src/androidTest/assets/identify-fixtures/`, named `<kb-species-id>__NN.jpg` (supports multiple
      independent photos per species without colliding with the existing single-photo `<kb-species-id>.jpg`
      fixtures). Center-crop to square, scale to **480×480**, re-encode **JPEG q80** to match the existing
      fixtures exactly. *(8 added: snake×2, ficus-elastica, aloe-vera, aglaonema, dieffenbachia, schefflera,
      kalanchoe — all manually vetted; snake plant now has 3 base photos)*
- [x] Replace/extend the freeform `identify-fixtures/LICENSE.txt` into a **machine-checkable attribution
      manifest** (filename, expected KB species id, source URL, author, license, license URL, acquisition
      date, notes) under a `PLANTPOTTING-0011` banner. *(added `fixture-manifest.tsv`; LICENSE.txt prose retained)*
- [x] **Log scarcity honestly** in `docs/sprints/evidence/PLANTPOTTING-0011/fixture-manifest-summary.md`:
      every species that could not be sourced cleanly, plus rejected near-misses where the license wasn't
      clean enough. No silent skips. *(pothos-2nd, poinsettia, anthurium, hedera-helix, dracaena-marginata logged)*
- [x] Add a **fixture-license cross-check** test (mirror `ReferenceImageManifestTest`) that fails when any
      `identify-fixtures/*.jpg` lacks a manifest row **or declares anything other than CC0/public-domain**.
      *(`FixtureLicenseManifestTest`; 4 pre-0011 CC BY-SA fixtures grandfathered by explicit allowlist —
      the AIY anchor `monstera-deliciosa.jpg` can't be swapped — new fixtures enforced CC0/PD)*
- [x] Add a **fixture integrity** test that decodes every fixture (nonzero dimensions), verifies the
      expected `<kb-species-id>` resolves in the KB, and marks whether the species is reachable by the active
      `plant_class_map.json` mapping (in-vocab vs not). *(`FixtureIntegrityTest`)*

**1b. Synthetic perturbation generator (deterministic, in-process, network-free).**
- [x] Add a perturbation helper under `app/src/androidTest/.../identify/` (e.g. `FixturePerturbations.kt`)
      that takes a decoded `Bitmap` and emits a fixed, documented family: **blur** (set radius), **center /
      off-center crop** (zoom), **rotate** (±15°, ±90°), **brightness/contrast** shift (±). Android-graphics
      only (`Canvas`/`Matrix`/`ColorMatrix` / box-blur) — **no new dependency, no network.** Use **fixed
      parameters** (no RNG) so runs are reproducible.
- [x] Determinism + label-preservation guard: the generator perturbs pixels reproducibly and never changes
      the expected `<kb-species-id>` (label is inherited from the source fixture). *(`FixturePerturbationsTest`)*

**1c. Scorecard harness + BEFORE number.**
- [x] Add an instrumented test (e.g. `AccuracyEvalTest`, reusing `ModelSwapEvaluationTest`'s `probeModel`
      plumbing) that, **for the production model only**, runs each clean fixture *and* each perturbation
      through the production preprocessor → facade → `ModelScoreMapper`, recording per row: expected id,
      raw top-1 label+score, raw top-2 label+score, top1-top2 margin, mapped top-1, mapped top-3, route
      (`high-conf`/`low-conf`), `confident_wrong` (route==high-conf AND mapped top-1 ≠ expected),
      perturbation kind, latency, failure. *(also sweeps squash/center_crop/tta5 in one run so Phase 2+3
      decisions compute offline from one CSV)*
- [x] Emit `accuracy-eval.csv` (per-row) + `accuracy-eval-summary.md` (aggregate) to the external files dir
      + logcat. The summary prints, for **clean**, **each perturbation family**, and **overall**: top-1,
      top-3, confident-wrong rate, abstain rate, median + worst latency — plus an **in-vocab-only** cut and a
      **per-base-image-averaged** cut (so one heavily-perturbed photo can't dominate).
- [x] Add a **CSV-schema guard** test so future evidence files can't silently drop the `confident_wrong` or
      `margin` columns. *(`AccuracyEvalCsvSchemaTest`; header-only CSV committed so the schema is locked now)*
- [x] Keep the **AIY baseline anchor** assertions in `OnDeviceModelRealInterpreterTest` unchanged (drift
      guard); do not weaken them.
- [x] Run on GMD `pixel6Api34` in CI; pull `accuracy-eval.csv` + `-summary.md` and **commit them as the
      documented BEFORE number** (thresholds + preprocessing untouched at this point). *(ran on the LOCAL
      Pixel_6_API_34 emulator — the "CI-only" assumption was wrong. BEFORE squash: top-1 0.654, top-3 0.796,
      **confident-wrong 0.254**, abstain 0.092. 20 fixtures/14 species, 720 rows committed.)*

### Phase 2 — IMPROVE raw accuracy (preprocessing; decision-driven; lock the shipping pipeline)
> Behind the seam in `ImagePreprocessor` / `OnDevicePlantIdentifier`. Each lever is a manifest-gated option
> defaulting to current behaviour; **keep only what the harness shows measurably improves top-1, drop the
> rest.** Adopt/drop is judged on top-1 (threshold-independent), so this phase precedes abstention tuning.
- [x] **Name the control.** Today `ImagePreprocessor` does a non-aspect-preserving
      `ResizeOp(inputSize, inputSize, BILINEAR)` — a squash. Treat that as the explicit A/B control.
- [x] **Center-crop vs squash.** Add a center-crop-then-resize option behind a new optional manifest field
      `preprocess_mode: "squash" | "center_crop"` (default `"squash"`). Evaluate it on the base + perturbed
      set (MobileNetV2 TF-Hub convention typically expects center-crop — measure it). *(code + harness done;
      ADOPT/DROP awaits CI scorecard)*
- [x] **Multi-crop / TTA.** Add an optional `ImagePreprocessor.preprocessVariants(jpeg): List<...>`
      (center + 4 corner crops, optional horizontal flip) and have `OnDevicePlantIdentifier` average the
      **softmax score vectors** across variants before `mapper.map(...)`. Gate behind a manifest `tta` count
      (default `1` = current). Measure median + **worst-case** latency (existing median-of-5 protocol).
      *(code + harness done; ADOPT/DROP awaits CI scorecard)*
- [x] **Orientation / EXIF.** Verify orientation is handled before crop (phone captures are often rotated);
      add normalisation only if the `rotate ±90°` perturbation rows show it helps. Operate on the decoded
      bitmap inside the preprocessor — no camera-path contract change. *(measured via rotate rows; TTA-6
      recovers most rotation loss — cw 0.288→0.138 — so NO separate EXIF lever added; fixtures carry no
      EXIF tag so it can't be measured directly. Revisit on-device.)*
- [x] **Decision log** `preprocessing-decision.md`: for each lever record top-1 before/after, confident-wrong
      before/after, median + worst latency before/after, and **ADOPT / DROP** with the number that drove it.
      **TTA hurdle (both critiques):** adopt TTA only if it beats **center-crop** (not just squash) by a
      clear margin at acceptable latency; otherwise leave it wired-but-disabled. *(DROP center_crop ≡ squash;
      ADOPT TTA — N=6 incl. principal-requested full-frame view; cw 0.254→0.113.)*
- [x] Flip the manifest default **only** for adopted levers; leave the rest wired-but-off. This **locks the
      shipping preprocessing pipeline** — Phase 3 tunes thresholds against it. *(production manifest:
      preprocess_mode=squash, tta=6; data-class defaults stay 1/squash for other models.)*

### Phase 3 — ABSTAIN (route confident-wrong → picker; tune once on the shipping pipeline)
**3a. Characterize, then add the lever (default-disabled).**
- [x] **Before changing policy**, add JVM unit coverage in `ModelScoreMapperTest` pinning the *existing*
      paths: high-confidence-direct, margin branch, low-confidence, confident-unmapped-top, and per-species
      override behaviour (lock current behaviour first). *(already pinned in `ModelScoreMapperTest` +
      `ModelScoreMapperPerSpeciesThresholdTest`; abstain tests assert the 0f no-op against them)*
- [x] Extend `ModelManifest.Thresholds` with one new optional field `highConfidenceAbstainMargin` (JSON
      `high_confidence_abstain_margin`), default **`0f` (disabled)**; parse with `?: 0f` in
      `ModelManifestReader.parse`. Update `ModelManifestTest` / `ModelManifestDtypeContractTest` for the new
      optional field.
- [x] In `ModelScoreMapper.map`, after computing the high-conf verdict, add an **abstention veto**: if the
      verdict would be high-confidence but `(bestProb - secondProb) < highConfidenceAbstainMargin`,
      **downgrade to low-confidence** (route to the picker with the same mapped candidates). At `0f` this is
      a no-op. Keep the existing margin-*branch* intact; this is an additional gate, not a replacement.
- [x] JVM unit tests for the new gate on **synthetic score vectors** (no device): (i) high prob + tiny
      top1-top2 margin → abstains; (ii) high prob + wide margin → stays high-conf; (iii) margin `0f` default
      → identical to today (regression). Makes abstention logic measurable on the JVM. *(`ModelScoreMapperAbstainMarginTest`)*

**3b. Tune the gate against the Phase-1/Phase-2 scorecard (evidence-driven, held-out).**
- [x] Re-run the scorecard on the **locked shipping pipeline** (post-Phase-2) to get the threshold-tuning
      baseline. Sweep candidate settings: raise global `high_confidence_plain` (currently 0.55),
      tighten/relax the margin branch (`high_confidence_margin_min` 0.45 / `high_confidence_margin_delta`
      0.18), set `high_confidence_abstain_margin`. Tabulate confident-wrong rate vs abstain rate per setting.
      *(offline sweep over the locked tta6 rows — abstention is deterministic post-processing of raw margins)*
- [x] Choose the setting that **minimises confident-wrong within an explicitly stated abstain-rate budget**
      (state the budget, e.g. "accept up to X% more low-conf routing"). **Prefer global changes**; document
      rejected threshold candidates in `docs/kb/ml-mapping-notes.md`. *(principal chose `abstain_margin 0.30`;
      budget = pick-manually up to ~0.275. Global change, no per-species. cw 0.254→0.083.)*
- [x] **Held-out validation (D2):** report the chosen setting's confident-wrong on the **perturbation rows
      it was not tuned against** and on a **leave-one-species-out** split — those are the honest
      generalisation numbers. Document the split in the summary. *(tune-on-clean→eval-on-perturbation cw 0.091;
      LOSO 0.056–0.093 mean 0.083; see abstention-before-after.md)*
- [x] Seed a `per_species_thresholds` entry **only** if a single in-vocab species is a chronic
      confident-wrong offender with **multiple independent clean base photos** AND a per-class value fixes it
      without harming others on the eval set; otherwise leave `per_species_thresholds: {}`. Respect
      `PerSpeciesThresholdsContractTest` / `ModelScoreMapperPerSpeciesThresholdTest`. Document either way.
      *(none met the 0006 bar — left `{}`; rationale in abstention-before-after.md)*
- [x] Apply the chosen settings to
      `app/src/main/assets/ml/house_plant_species_mobilenetv2/model_manifest.json` (+ its
      `_comment_thresholds`). Re-run the GMD scorecard and commit the **AFTER** summary; the abstention
      before/after table (confident-wrong ↓, abstain ↑) is this phase's headline deliverable. *(manifest set
      to tta=6 + abstain_margin 0.30; AFTER derived from the committed raw CSV, gate unit-pinned by
      ModelScoreMapperAbstainMarginTest; abstention-before-after.md committed)*

### Phase 4 — 0010-review fold-ins (small, independent)
- [x] **Thicken the confidence bar.** In `ResultScreen.kt` (the `LinearProgressIndicator` at line 123), bump
      the bar height via `Modifier.height(...)`, keeping `progress` and the
      `ResultScreenTags.CONFIDENCE_PCT` / `CONFIDENCE_BAR` test tags intact. Update/confirm the
      `ResultScreen` Compose test that asserts the bar. *(now 12dp + rounded corners; `ResultScreenConfidenceTest` green)*
- [x] **Replace botanical-plate reference images with photographs where a clean one exists.** Target the
      plates from the 0010 handoff: peace lily, poinsettia, parlor palm, dracaena, philodendron-pink-princess.
      For each, source a CC0/PD **photograph** (`source-reference-images.ps1`); if found, replace the WebP
      under `res/drawable-nodpi/` (≤480px, same `PlantImageResolver` key). **If no clean photo exists, leave
      the plate and document why** in `docs/licenses/reference-images.md`. *(pink-princess SWAPPED to a CC0
      photo; other 4 have no CC0/PD photograph — plates retained + documented)*
- [x] Update `docs/licenses/reference-images.md` for swapped images; keep
      `ReferenceImageManifestTest.everyBundledReferenceImageHasAManifestEntry` GREEN.

### Phase 5 — Version bump, gates, delivery
- [x] Bump `versionCode` 4→5 and `versionName` 0.4.0→0.5.0 in `app/build.gradle.kts`.
- [ ] Full gate set GREEN: `./gradlew :app:testDebugUnitTest`, `verifyNoNetworking`,
      `bash scripts/check-stub-isolation.sh`, lint (`abortOnError`), `:app:compileDebugAndroidTestKotlin`.
      GMD `pixel6Api34` runs in CI (no local emulator) — pull its `gradle-reports` + the scorecard artifacts.
- [ ] Build the debug APK and copy to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\`; **verify
      it landed from PowerShell** (sandbox-overlay caveat — `adb` not on PATH). Name it
      `app-debug-PLANTPOTTING-0011-v0.5.0.apk`.
- [ ] On-device sanity (principal): a previously confident-wrong capture now abstains to the picker rather
      than showing a confident-wrong card; the confidence bar reads thicker; swapped reference photos render.

### Phase 6 — Documentation, evidence & close
- [ ] Update `docs/kb/ml-mapping-notes.md §PLANTPOTTING-0011` with the calibration story: the BEFORE
      confident-wrong number, preprocessing ADOPT/DROP decisions + numbers, the abstention setting chosen
      (and rejected candidates), the held-out generalisation number, and the AFTER number — framed as
      *measured-under-clean-CC-conditions + synthetic-robustness, NOT real-world-accuracy-solved*.
- [ ] Ensure all evidence artifacts are committed under `docs/sprints/evidence/PLANTPOTTING-0011/`
      (BEFORE/AFTER scorecard CSV + summaries, fixture-manifest-summary, preprocessing-decision, abstention
      before/after table).
- [ ] Final guard sweep recorded in the evidence README: unit suite, instrumented compile-clean,
      `verifyNoNetworking`, `check-stub-isolation.sh`, `HousePlantClassMapValidationTest` (Pilea absent),
      AIY baseline anchor, fixture + reference license cross-checks — **all GREEN**.

## Sequencing & rationale

```
Phase 0 (evidence dir + baseline green)
Phase 1 MEASURE ── 1a fixtures ┐
                   1b perturb  ┴─> 1c scorecard ─> BEFORE number (current thresholds + squash)
Phase 2 IMPROVE (preprocessing; adopt/drop on top-1) ─> LOCK shipping pipeline
Phase 3 ABSTAIN (tune thresholds ONCE on the locked pipeline; held-out validation) ─> AFTER number
Phase 4 fold-ins (UI + ref images) ── independent, any time
Phase 5 version bump + gates + APK
Phase 6 docs + evidence + close
```

- **MEASURE first** — no honest tuning or preprocessing verdict without the scorecard (D1).
- **IMPROVE before ABSTAIN** — preprocessing shifts the score distribution; deciding it first (on
  threshold-independent top-1) lets thresholds be tuned **once** against the shipping pipeline, closing the
  re-tune circularity all three drafts left open (D1, CLAUDE critique).
- **Phase 4 is fully independent** (UI + assets) and can run in parallel; reference-image replacement reuses
  the Phase-1 CC0/PD sourcing discipline.
- **Defaults-disabled rule:** every manifest/threshold/preprocessing change defaults to current behaviour so
  each commit keeps AIY + existing tests green; flip defaults only once this sprint's evidence justifies it
  (D3).

## Risks & mitigations

| # | Risk | Mitigation |
|---|------|-----------|
| 1 | **★ Overfitting thresholds/preprocessing to a tiny clean fixture set** (central risk). | Multiple base photos per species (`__NN.jpg`); per-base-image-averaged metrics; **held-out** leave-one-species-out + perturbation-rows-not-tuned-against as the honest number; prefer global over per-species; report confident-wrong, not just top-1 (D2). |
| 2 | License-clean photos scarce (0008: ~1–6.8% clean) — may not reach +8–16. | Accept a smaller real set; lean on synthetic perturbations for robustness coverage; **log every un-sourceable species** (no silent truncation); CC0/PD-only, no loosening. |
| 3 | Synthetic perturbations ≠ real-world messiness. | Label them *synthetic-robustness*; report clean numbers separately; frame the win as "confident-wrong ↓ under stress," not "real-world accuracy solved." |
| 4 | Eval set under-represents the principal's actual failures (clean ≠ messy). | State this limitation explicitly in intent/notes/acceptance; the on-device APK check (Phase 5) is the real-world spot-check the harness can't be. |
| 5 | TTA latency vs benefit. | Median + worst-case latency in-harness; adopt only if it beats **center-crop** at acceptable cost; N ≤ 5; default off if marginal. |
| 6 | Center-crop removes diagnostic plant context. | Evaluate squash control vs center-crop vs crop-ensemble on the same fixtures before adopting. |
| 7 | Accidentally changing AIY baseline or the frozen seam. | New manifest fields optional + default-disabled; AIY anchor assertions verbatim; `PlantIdentifier`/`IdentificationResult`/`IdSource` untouched; existing contract tests stay green. |
| 8 | Manifest schema drift breaks the parser. | `Json { ignoreUnknownKeys = true }` already set; parse new fields with safe defaults; manifest test for the new optional fields. |
| 9 | Abstention makes the app feel less capable. | Acceptance is framed around reducing confident-wrong (not raw accuracy); report the low-conf routing increase as an explicit, budgeted tradeoff; top-3 candidates preserved for manual pick. |
| 10 | GMD transient ~1h timeout (known flake — Settings-pollution lesson). | Keep new instrumented tests self-contained (no real-app intents); pull `gradle-reports` JUnit XML + per-test logcat; rerun the GMD job rather than weakening assertions. |
| 11 | Dropbox APK copy doesn't land (sandbox overlay). | Verify the copy from PowerShell before claiming delivery (memory `apk_delivery_dropbox`). |

## Acceptance criteria

- [ ] An expanded **CC0/PD-only** fixture set (actual count + every un-sourceable species documented), each
      fixture license-attributed and enforced by a cross-check test that rejects non-CC0/PD; a fixture
      integrity test (decode + KB-resolution + in-vocab reachability) passes.
- [ ] A deterministic, network-free synthetic-perturbation generator (blur/crop/rotate/brightness) with a
      determinism + label-preservation guard.
- [ ] A committed **BEFORE scorecard** (`accuracy-eval.csv` + `accuracy-eval-summary.md`) reporting top-1,
      top-3, **confident-wrong rate**, abstain rate, median + worst latency — separately for clean and each
      perturbation family, with an in-vocab-only cut and a per-base-image-averaged cut — for the production
      model, AIY baseline anchor intact, protected by a CSV-schema guard.
- [ ] A `preprocessing-decision.md` recording ADOPT/DROP for center-crop, TTA, and orientation, each with
      top-1 / confident-wrong / latency numbers; TTA adopted only if it beats center-crop; only adopted
      levers have their manifest default flipped; the shipping pipeline is locked before threshold tuning.
- [ ] A `high_confidence_abstain_margin` lever in `ModelManifest.Thresholds` + `ModelScoreMapper`,
      default-disabled (AIY/baseline byte-for-byte unchanged), with JVM unit tests proving abstain/keep/no-op,
      and characterization tests pinning the pre-existing mapper paths.
- [ ] A committed **AFTER scorecard** showing **confident-wrong rate down** vs BEFORE, with the accepted
      abstain-rate cost stated, settings applied to the production `model_manifest.json`, and validated on
      the **held-out** (perturbation / leave-one-species-out) split to show it isn't overfit.
- [ ] Confidence bar visibly thicker on `ResultScreen` (test green); called-out botanical plates replaced
      with CC0/PD photos where one exists (`ReferenceImageManifestTest` green), remaining plates documented.
- [ ] `versionCode`/`versionName` bumped (4→5 / 0.4.0→0.5.0); a debug APK built and **confirmed copied** to
      the Dropbox APK folder.
- [ ] `PlantIdentifier`/`IdentificationResult`/`IdSource` byte-for-byte unchanged; Pilea still unmapped
      (`HousePlantClassMapValidationTest` green); `verifyNoNetworking` + `check-stub-isolation.sh` green; unit
      suite + lint green; instrumented sources compile-clean; AIY baseline anchor unchanged.
- [ ] `docs/kb/ml-mapping-notes.md §PLANTPOTTING-0011` records the before→after calibration narrative,
      explicitly separating **measured-under-clean-CC-conditions + synthetic-robustness** from
      **real-world-accuracy** (which this sprint does not claim to solve); all evidence committed under
      `docs/sprints/evidence/PLANTPOTTING-0011/`.
