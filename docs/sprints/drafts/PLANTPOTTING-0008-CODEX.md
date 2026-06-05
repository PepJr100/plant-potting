# PLANTPOTTING-0008 - Training-data availability spike

PLANTPOTTING-0008 is a feasibility spike for a possible later fine-tuning sprint. It
does not train, convert, swap, or ship a model. It answers one gate question: is there
enough free, license-clean image data to teach the current production model
`house_plant_species_mobilenetv2` the six KB species it cannot currently identify, and
to improve the pothos / Pilea boundary where both classes already exist but pothos is
misclassified as Pilea?

The production model remains `house_plant_species_mobilenetv2` behind the existing
single `ACTIVE_MODEL_ROOT` BuildConfig switch. The Android app remains network-free.

## Goals

- [ ] Produce a per-species sourcing report at
      `docs/sprints/evidence/PLANTPOTTING-0008/training-data-availability-report.md`
      covering `monstera-adansonii`, `philodendron-hederaceum`,
      `philodendron-pink-princess`, `ficus-lyrata`,
      `chlorophytum-comosum`, `hoya-carnosa`, `epipremnum-aureum`, and
      Pilea.
- [ ] Produce a machine-readable source-count table at
      `docs/sprints/evidence/PLANTPOTTING-0008/source-counts.csv` with one row per
      species/source/query/license bucket.
- [ ] Decide GO / NO-GO per species and overall using the sprint threshold:
      150-300 license-clean images/species is comfortable, 50-100 images/species is
      the floor, and the pothos / Pilea boundary needs 150-300 usable hard examples
      for each side.
- [ ] Confirm whether train/val/test splits can be kept disjoint from the eight existing
      androidTest fixtures in `app/src/androidTest/assets/identify-fixtures/`.
- [ ] Document a scoped future fine-tuning sprint if the data gate is GO, including
      sourcing, disjoint splits, transfer learning, TFLite export, and reuse of the
      existing model-swap evaluation harness.
- [ ] Document the paid-dataset and self-shot-photo fallback if the data gate is NO-GO
      for any blocking species.
- [ ] Fix the camera UX bug where the shutter remains greyed out after navigating back
      from the results screen, and cover the fix with a focused test.

## Non-Goals / Scope Boundaries

- [ ] Do not train, fine-tune, calibrate, quantize, or evaluate a new model in this
      sprint.
- [ ] Do not swap the production model, add a new model bundle, or change the default
      `ACTIVE_MODEL_ROOT`.
- [ ] Do not change the `PlantIdentifier` / `IdentificationResult` seam.
- [ ] Do not edit `species.json`, `archetypes.json`, potting recommendation logic, or KB
      mappings.
- [ ] Do not bulk-download image data, commit sample images, add images to APK assets, or
      add training data to the repo.
- [ ] Do not invoke any research API script from a Gradle app task, instrumentation test,
      app runtime path, or on-device code.
- [ ] Do not add production networking, remote model downloads, telemetry, cloud
      identification, or source-specific SDKs to the app.
- [ ] Do not bump AGP, Kotlin, Compose, Hilt, CameraX, TFLite, or other app dependency
      versions.
- [ ] Do not broaden the idea-inbox UX work beyond the shutter re-enable bug.

## Phase 1 - Research Artifact Skeleton

- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0008/README.md` describing the spike,
      the no-download rule, the source APIs/UI used, and the meaning of GO / NO-GO.
- [ ] Create
      `docs/sprints/evidence/PLANTPOTTING-0008/training-data-availability-report.md`
      with a section for each target: `monstera-adansonii`,
      `philodendron-hederaceum`, `philodendron-pink-princess`,
      `ficus-lyrata`, `chlorophytum-comosum`, `hoya-carnosa`,
      `epipremnum-aureum`, and Pilea.
- [ ] Add a report table template with columns: `species_id`, `scientific_name`,
      `source`, `query_used`, `license_clean_count`, `license_types`,
      `attribution_feasible`, `provenance_fields_available`, `taxonomic_pitfalls`,
      `cultivar_notes`, `hard_example_notes`, `fixture_disjoint_notes`,
      `count_confidence`, and `recommendation`.
- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0008/source-counts.csv` with the same
      source-count columns so the report can be checked and diffed.
- [ ] Create `docs/sprints/evidence/PLANTPOTTING-0008/query-log.md` to record exact UI
      filters, API URLs, dates queried, source quirks, and any manual eyeball-check notes.
- [ ] Create
      `docs/sprints/evidence/PLANTPOTTING-0008/future-finetuning-outline.md` with empty
      GO and NO-GO sections that are filled only after the count matrix is complete.
- [ ] Add a local `.gitignore` under `docs/sprints/evidence/PLANTPOTTING-0008/` that
      ignores any accidental `*.jpg`, `*.jpeg`, `*.png`, `*.webp`, `downloads/`,
      `samples/`, `raw/`, and API cache files.

## Phase 2 - Source Method And License Rules

- [ ] Define the license-clean set in the report: CC0, CC-BY, CC-BY-SA, and other
      redistribution/training-compatible CC licenses accepted by the project; mark
      NC, ND, All Rights Reserved, unknown, and missing-license records as excluded.
- [ ] Record attribution feasibility requirements against the existing fixture pattern in
      `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`: title, depicts/source
      taxon, source page, original file or observation URL, author, date if available,
      retrieved date, modifications, license, and attribution string.
- [ ] Record that research may use API counts, source UI counts, and tiny off-repo manual
      checks, but no source images may be committed or shipped.
- [ ] Add source-specific counting notes for iNaturalist research-grade observations with
      CC licenses and explain whether observation-level and photo-level licenses both need
      checking.
- [ ] Add source-specific counting notes for GBIF occurrence/media records and explain how
      to avoid counting records without image media or usable media licenses.
- [ ] Add source-specific counting notes for Wikimedia Commons categories/search and
      explain how Commons file licenses and attribution fields map to fixture provenance.
- [ ] Add source-specific counting notes for Flickr-CC search and explain how license
      filters and per-photo attribution are verified.
- [ ] Add a row for "other obvious CC/CC0 source" only when a source has queryable counts,
      stable source URLs, per-image license metadata, and feasible attribution.

## Phase 3 - Species And Taxonomy Setup

- [ ] Record accepted scientific name, likely synonyms, and search terms for
      `monstera-adansonii`, including `Monstera adansonii`, "Swiss cheese vine", and
      pitfalls around `Monstera deliciosa` juvenile fenestrated leaves.
- [ ] Record accepted scientific name, likely synonyms, and search terms for
      `philodendron-hederaceum`, including `Philodendron hederaceum`,
      `Philodendron scandens`, "heartleaf philodendron", and pothos/scindapsus
      lookalike pitfalls.
- [ ] Record accepted scientific name, cultivar-specific search terms, and viability
      warning for `philodendron-pink-princess`, including `Philodendron erubescens
      'Pink Princess'`, "Pink Princess philodendron", and the need to exclude generic
      `Philodendron erubescens` records without cultivar evidence.
- [ ] Record accepted scientific name, synonyms, and search terms for `ficus-lyrata`,
      including `Ficus lyrata`, "fiddle-leaf fig", and cultivar/form notes for compact
      houseplant versus tree-form images.
- [ ] Record accepted scientific name, synonyms, and search terms for
      `chlorophytum-comosum`, including `Chlorophytum comosum`, "spider plant", and
      variegated cultivar notes.
- [ ] Record accepted scientific name, synonyms, and search terms for `hoya-carnosa`,
      including `Hoya carnosa`, wax plant, and cultivar notes where variegated or compacta
      forms may not represent the base class cleanly.
- [ ] Record search terms for the pothos side of the boundary:
      `Epipremnum aureum`, pothos, golden pothos, devil's ivy, and exclude true
      `Pothos` genus records when they are not `Epipremnum aureum`.
- [ ] Record search terms for the Pilea side of the boundary using the exact production
      model class and KB fixture context, including `Pilea peperomioides` if that is the
      mapped class, and avoid generic `Pilea` images that do not match the model class.
- [ ] Verify that the eight existing fixtures in
      `app/src/androidTest/assets/identify-fixtures/` are listed as excluded split seeds:
      `crassula-ovata.jpg`, `dracaena-trifasciata.jpg`, `epipremnum-aureum.jpg`,
      `goeppertia-orbifolia.jpg`, `monstera-deliciosa.jpg`, `phalaenopsis.jpg`,
      `spathiphyllum-wallisii.jpg`, and `zamioculcas-zamiifolia.jpg`.

## Phase 4 - Per-Source Count Collection

- [ ] Query iNaturalist for research-grade, photo-backed, CC-licensed observations of
      `monstera-adansonii`; record count, license buckets, query URL, attribution fields,
      and Monstera lookalike risk.
- [ ] Query GBIF for image-backed, license-clean `monstera-adansonii` records; record
      count, license buckets, query URL, attribution fields, and duplicate overlap risk.
- [ ] Query Wikimedia Commons for `monstera-adansonii`; record license-clean file count,
      categories/search terms, attribution fields, and juvenile `Monstera deliciosa`
      exclusion notes.
- [ ] Query Flickr-CC for `monstera-adansonii`; record license-clean count, filters,
      attribution fields, and manual ambiguity notes.
- [ ] Query iNaturalist for research-grade, photo-backed, CC-licensed observations of
      `philodendron-hederaceum`; record count, license buckets, query URL, attribution
      fields, and synonym handling.
- [ ] Query GBIF for image-backed, license-clean `philodendron-hederaceum` records;
      record count, license buckets, query URL, attribution fields, and duplicate overlap
      risk.
- [ ] Query Wikimedia Commons for `philodendron-hederaceum`; record license-clean file
      count, categories/search terms, attribution fields, and pothos/scindapsus confusion
      notes.
- [ ] Query Flickr-CC for `philodendron-hederaceum`; record license-clean count, filters,
      attribution fields, and manual ambiguity notes.
- [ ] Query iNaturalist for research-grade, photo-backed, CC-licensed observations of
      `philodendron-pink-princess`; record count only when the cultivar is explicit in
      taxon, observation text, tags, or source category.
- [ ] Query GBIF for image-backed, license-clean `philodendron-pink-princess` or
      `Philodendron erubescens 'Pink Princess'` records; record whether GBIF supports a
      cultivar-specific count or only generic species counts.
- [ ] Query Wikimedia Commons for `philodendron-pink-princess`; record cultivar-specific
      license-clean file count and exclude generic `Philodendron erubescens` rows unless
      the file/category proves Pink Princess.
- [ ] Query Flickr-CC for `philodendron-pink-princess`; record license-clean count,
      filters, attribution fields, and cultivar-confidence notes.
- [ ] Query iNaturalist for research-grade, photo-backed, CC-licensed observations of
      `ficus-lyrata`; record count, license buckets, query URL, attribution fields, and
      indoor/outdoor form notes.
- [ ] Query GBIF for image-backed, license-clean `ficus-lyrata` records; record count,
      license buckets, query URL, attribution fields, and duplicate overlap risk.
- [ ] Query Wikimedia Commons for `ficus-lyrata`; record license-clean file count,
      categories/search terms, attribution fields, and fiddle-leaf fig ambiguity notes.
- [ ] Query Flickr-CC for `ficus-lyrata`; record license-clean count, filters,
      attribution fields, and manual ambiguity notes.
- [ ] Query iNaturalist for research-grade, photo-backed, CC-licensed observations of
      `chlorophytum-comosum`; record count, license buckets, query URL, attribution
      fields, and cultivar variation notes.
- [ ] Query GBIF for image-backed, license-clean `chlorophytum-comosum` records; record
      count, license buckets, query URL, attribution fields, and duplicate overlap risk.
- [ ] Query Wikimedia Commons for `chlorophytum-comosum`; record license-clean file count,
      categories/search terms, attribution fields, and spider plant cultivar notes.
- [ ] Query Flickr-CC for `chlorophytum-comosum`; record license-clean count, filters,
      attribution fields, and manual ambiguity notes.
- [ ] Query iNaturalist for research-grade, photo-backed, CC-licensed observations of
      `hoya-carnosa`; record count, license buckets, query URL, attribution fields, and
      cultivar/form notes.
- [ ] Query GBIF for image-backed, license-clean `hoya-carnosa` records; record count,
      license buckets, query URL, attribution fields, and duplicate overlap risk.
- [ ] Query Wikimedia Commons for `hoya-carnosa`; record license-clean file count,
      categories/search terms, attribution fields, and wax plant cultivar notes.
- [ ] Query Flickr-CC for `hoya-carnosa`; record license-clean count, filters,
      attribution fields, and manual ambiguity notes.
- [ ] Query iNaturalist for research-grade, photo-backed, CC-licensed observations of
      `epipremnum-aureum`; record count, license buckets, query URL, attribution fields,
      and hard-example availability for pothos/Pilea boundary work.
- [ ] Query GBIF for image-backed, license-clean `epipremnum-aureum` records; record
      count, license buckets, query URL, attribution fields, and duplicate overlap risk.
- [ ] Query Wikimedia Commons for `epipremnum-aureum`; record license-clean file count,
      categories/search terms, attribution fields, and exclusion notes for true `Pothos`
      genus records.
- [ ] Query Flickr-CC for `epipremnum-aureum`; record license-clean count, filters,
      attribution fields, and hard-example notes.
- [ ] Query iNaturalist for research-grade, photo-backed, CC-licensed observations of the
      production Pilea class; record count, license buckets, query URL, attribution
      fields, and hard-example availability.
- [ ] Query GBIF for image-backed, license-clean records of the production Pilea class;
      record count, license buckets, query URL, attribution fields, and duplicate overlap
      risk.
- [ ] Query Wikimedia Commons for the production Pilea class; record license-clean file
      count, categories/search terms, attribution fields, and generic-Pilea exclusion
      notes.
- [ ] Query Flickr-CC for the production Pilea class; record license-clean count,
      filters, attribution fields, and hard-example notes.

## Phase 5 - Count Quality, Dedupe, And Split Feasibility

- [ ] Estimate cross-source duplicate risk for each species by checking whether GBIF
      records mirror iNaturalist media and marking duplicate-prone source pairs in
      `source-counts.csv`.
- [ ] For each species, mark whether enough unique authors/observations appear to support
      disjoint train/val/test splits by observation or author rather than by image crop.
- [ ] For each of the six OOV species, classify count confidence as `high`, `medium`, or
      `low` based on source metadata quality, taxon precision, and duplicate risk.
- [ ] For `philodendron-pink-princess`, separately count `cultivar-proven` images and
      generic philodendron images, and base the recommendation only on
      `cultivar-proven` images.
- [ ] For pothos, estimate the subset of license-clean images that are useful hard
      examples: trailing vines, heart-shaped leaves, variegation, juvenile leaves, and
      images visually close to Pilea only when still taxonomically pothos.
- [ ] For Pilea, estimate the subset of license-clean images that are useful hard
      examples: coin-shaped leaves, petiole-centered leaves, juvenile plants, cluttered
      houseplant scenes, and images visually close to pothos only when still
      taxonomically Pilea.
- [ ] Confirm that no existing androidTest fixture image is counted toward training data,
      validation data, or test data in the future outline.
- [ ] Record whether each source exposes enough provenance metadata to generate
      fixture-style provenance blocks for every selected training image in a later
      sprint.
- [ ] Record whether each source's license metadata is available per image or only at a
      weaker aggregate level, and downgrade count confidence where per-image license
      verification is not practical.

## Phase 6 - Recommendation And Future Sprint Outline

- [ ] Write a per-species recommendation for `monstera-adansonii`: `GO`,
      `CONDITIONAL GO`, or `NO-GO`, with count totals, best sources, license notes, split
      feasibility, and taxonomic risks.
- [ ] Write a per-species recommendation for `philodendron-hederaceum`: `GO`,
      `CONDITIONAL GO`, or `NO-GO`, with count totals, best sources, license notes, split
      feasibility, and pothos/scindapsus confusion risks.
- [ ] Write a per-species recommendation for `philodendron-pink-princess`: `GO`,
      `CONDITIONAL GO`, or `NO-GO`, based only on cultivar-proven counts.
- [ ] Write a per-species recommendation for `ficus-lyrata`: `GO`,
      `CONDITIONAL GO`, or `NO-GO`, with count totals, best sources, license notes, split
      feasibility, and indoor/outdoor form risks.
- [ ] Write a per-species recommendation for `chlorophytum-comosum`: `GO`,
      `CONDITIONAL GO`, or `NO-GO`, with count totals, best sources, license notes, split
      feasibility, and cultivar variation risks.
- [ ] Write a per-species recommendation for `hoya-carnosa`: `GO`,
      `CONDITIONAL GO`, or `NO-GO`, with count totals, best sources, license notes, split
      feasibility, and cultivar/form risks.
- [ ] Write a boundary-fix recommendation for `epipremnum-aureum` and Pilea together,
      explicitly stating whether 150-300 hard examples for each side are realistic.
- [ ] Write the overall GO / NO-GO gate in
      `training-data-availability-report.md`, including which species block a full
      fine-tuning sprint if any.
- [ ] On GO, fill
      `docs/sprints/evidence/PLANTPOTTING-0008/future-finetuning-outline.md` with a
      scoped sourcing/download plan that lists source priority, per-image provenance
      capture, no-repo raw data storage, and manual QA steps.
- [ ] On GO, add a disjoint-split strategy to `future-finetuning-outline.md` requiring
      train/val/test separation by source observation or source file id and excluding all
      eight existing androidTest fixtures from training.
- [ ] On GO, add a transfer-learning approach to `future-finetuning-outline.md` using the
      existing `house_plant_species_mobilenetv2` backbone/head strategy where practical,
      without changing the `PlantIdentifier` / `IdentificationResult` contract.
- [ ] On GO, add export requirements to `future-finetuning-outline.md`: TFLite export,
      float16 and INT8 comparison, manifest updates, model asset provenance, and no
      default `ACTIVE_MODEL_ROOT` change until the later model-swap sprint approves it.
- [ ] On GO, add evaluation reuse requirements to `future-finetuning-outline.md`:
      `app/src/androidTest/.../identify/ModelSwapEvaluationTest.kt`,
      the `ACTIVE_MODEL_ROOT` switch, fixture/provenance conventions, and a new held-out
      fixture set that stays license-clean.
- [ ] On NO-GO or conditional GO, fill the fallback section with paid dataset options,
      self-shot photo requirements, estimated number of images still needed per species,
      rough collection cost, consent/release considerations, and schedule impact.
- [ ] Update `docs/sprints/results/PLANTPOTTING-0008.md` with the final gate decision,
      links to the evidence files, and a short statement that no training data or model
      assets were committed.

## Phase 7 - Camera Back-From-Results Shutter Fix

- [ ] Reproduce the bug by following the current flow: launch camera, ensure shutter is
      enabled, trigger `CameraViewModel.onCaptureReady(...)`, navigate to the result
      screen, press system/back navigation, and observe whether `CameraScreenTags.SHUTTER`
      is disabled or alpha-dimmed on return.
- [ ] Inspect `app/src/main/java/com/darkfactory/plantpotting/camera/CameraScreen.kt` and
      `app/src/main/java/com/darkfactory/plantpotting/camera/CameraViewModel.kt` to find
      whether stale `CameraUiState.Success`, `Capturing`, or `Identifying` state is
      preventing the shutter from returning to enabled.
- [ ] Add a narrow ViewModel or navigation-return hook that resets the camera state to
      `CameraUiState.Idle` only when returning from a completed result flow, while
      preserving `Failure` retry behavior and in-flight capture protection.
- [ ] Keep the fix confined to the camera screen, camera ViewModel state, or navigation
      glue that owns camera return state; do not touch `PlantIdentifier`,
      `IdentificationResult`, model assets, KB files, or recommendation logic.
- [ ] Add a focused test in the existing camera test area, preferring
      `app/src/androidTest/java/com/darkfactory/plantpotting/camera/CameraScreenSmokeTest.kt`
      or a sibling instrumentation test, that proves the shutter is enabled after
      navigating back from the results screen.
- [ ] Add a focused unit test in
      `app/src/test/java/com/darkfactory/plantpotting/camera/CameraViewModelTest.kt` if
      the fix introduces a ViewModel reset method or state transition.
- [ ] Verify the existing failure retry tests still pass:
      `CameraScreenTest.failureRetryReturnsToIdle` and
      `CameraViewModelTest.newCaptureClearsFailureState`.
- [ ] Verify existing bind-state assertions still pass:
      `CameraScreenBindStateTest.shutterIsDisabledAndBindProgressVisibleWhileImageCaptureIsNull`
      and `CameraScreenBoundStateTest.shutterIsEnabledAndBindProgressGoneWhenImageCaptureIsBound`.

## Phase 8 - Gates And Verification

- [ ] Run the focused camera unit tests with a portable Gradle command such as
      `./gradlew testDebugUnitTest --tests "*CameraViewModelTest*"`.
- [ ] Run the focused camera instrumentation tests on the configured emulator/device,
      including the new back-from-results shutter test and the existing camera smoke/bind
      tests.
- [ ] Run `./gradlew verifyNoNetworking` and record the result in
      `docs/sprints/results/PLANTPOTTING-0008.md`.
- [ ] Run `scripts/check-stub-isolation.sh` from a shell that supports the script and
      record the result in `docs/sprints/results/PLANTPOTTING-0008.md`.
- [ ] Run `./gradlew testDebugUnitTest` after the camera fix and record the result.
- [ ] Run `./gradlew ktlintCheck` after the camera fix and record the result.
- [ ] Confirm `git status` contains no committed or staged research images, model files,
      generated training data, API cache blobs, or KB edits.
- [ ] Confirm no Gradle task, app module source set, Android asset directory, or
      instrumentation test references the research API scripts or evidence-only count
      files.

## Sequencing And Dependencies

- [ ] Complete Phase 1 before count collection so every source query has a place to land.
- [ ] Complete Phase 2 before interpreting source counts so excluded licenses and
      attribution rules are consistent across sources.
- [ ] Complete Phase 3 before source queries so synonym/cultivar mistakes do not inflate
      counts.
- [ ] Complete Phase 4 before writing GO / NO-GO recommendations.
- [ ] Complete Phase 5 before the overall gate because duplicate risk, cultivar confidence,
      hard-example usefulness, and split feasibility can lower nominal counts.
- [ ] Complete Phase 6 before closing the spike so the next sprint has either a scoped
      fine-tuning outline or a documented fallback path.
- [ ] Implement Phase 7 independently of the research work, but land it in the same sprint
      only after confirming it does not touch model, KB, or networking surfaces.
- [ ] Run Phase 8 after both the report and camera fix are complete.

## Risks And Mitigations

- [ ] Mitigate inflated source counts by recording query URLs, license buckets,
      per-image-license availability, and duplicate-prone source overlap instead of
      trusting headline search result numbers.
- [ ] Mitigate cultivar drift for `philodendron-pink-princess` by using only
      cultivar-proven rows for the viability recommendation.
- [ ] Mitigate a false GO for pothos/Pilea by counting hard-example usefulness separately
      from general species image availability.
- [ ] Mitigate taxonomy ambiguity by recording synonyms and exclusion rules before
      querying each source.
- [ ] Mitigate attribution risk by requiring every source row to state whether fixture-style
      provenance blocks can be generated later.
- [ ] Mitigate repo bloat and license leakage by keeping all manual image checks off-repo
      and adding evidence-folder ignore rules for accidental media/cache files.
- [ ] Mitigate accidental app networking by keeping research scripts under
      `docs/sprints/evidence/PLANTPOTTING-0008/` or `scripts/`, never wiring them into
      Gradle app tasks, and running `verifyNoNetworking`.
- [ ] Mitigate camera regression risk by testing the back-from-results path plus existing
      idle, failure, bind-pending, and bind-ready shutter states.

## Acceptance Criteria

- [ ] `training-data-availability-report.md` exists and contains per-source,
      license-clean counts for all six OOV species plus `epipremnum-aureum` and the
      production Pilea class.
- [ ] `source-counts.csv` exists and includes `species_id`, `source`, `query_used`,
      `license_clean_count`, `license_types`, `attribution_feasible`,
      `taxonomic_pitfalls`, `cultivar_notes`, `hard_example_notes`,
      `fixture_disjoint_notes`, `count_confidence`, and `recommendation`.
- [ ] Every target species has a per-species GO / CONDITIONAL GO / NO-GO recommendation
      tied to the 150-300 comfortable threshold and 50-100 floor.
- [ ] The pothos / Pilea boundary recommendation states whether 150-300 hard examples for
      each side are available and sourceable with clean licenses.
- [ ] The report explicitly excludes the eight existing androidTest fixtures from any
      future train/val/test count.
- [ ] The report states whether every usable source can support provenance blocks matching
      `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`.
- [ ] The report documents taxonomy, synonym, and cultivar pitfalls for each target,
      including the Pink Princess cultivar constraint.
- [ ] `future-finetuning-outline.md` contains either a GO plan for a later fine-tuning
      sprint or a NO-GO fallback with paid/self-shot collection estimates.
- [ ] The camera shutter re-enables after navigating back from the results screen.
- [ ] The shutter fix is covered by a focused test and does not modify model assets,
      `ACTIVE_MODEL_ROOT`, `PlantIdentifier`, `IdentificationResult`, or KB files.
- [ ] `verifyNoNetworking` and `scripts/check-stub-isolation.sh` are green at sprint close.
- [ ] No bulk image data, sample images, model bundles, or research cache files are
      committed or shipped.
