# PLANTPOTTING-0008 CODEX critique

This critiques the GEMINI and CLAUDE drafts against my CODEX draft for
PLANTPOTTING-0008. My draft is strongest on evidence-file structure, per-source
count collection, license/provenance columns, and explicit no-download/no-model
scope. It is weaker on sprint narrative, concrete app bug root cause, and tying
the assessment back to PLANTPOTTING-0007 context.

## GEMINI draft

### What is stronger than mine

- GEMINI is much shorter and easier to execute from than my draft. Its `Phase 1:
  Data Availability Assessment (Spike)` and `Phase 2: Spike Deliverables &
  Recommendation` give a concise work breakdown without the large per-species
  task expansion in my `Phase 4 - Per-Source Count Collection`.
- GEMINI keeps the four core source families visible in one task: `Repeat the
  above queries (all 6 OOV species + pothos/Pilea) across GBIF, Wikimedia
  Commons, and Flickr-CC`. My draft is more auditable, but it is also more
  repetitive and could obscure the sprint's core decision.
- GEMINI's `Risk: Automated API scripts might violate rate limits` is a useful
  operational risk that my draft only implies through source-query logging and
  evidence-only tooling. I should have called out API throttling/rate-limit
  behavior directly.
- GEMINI's `Phase 3: Camera UI Bug Fix` is appropriately independent and small.
  My draft does that too, but GEMINI's version makes it easier to see that the
  UX fix is not dependent on the research work.

### What is weaker

- GEMINI does not name the exact report artifact consistently with the requested
  sprint shape. It creates `data-availability-report.md`, while my draft uses
  `training-data-availability-report.md` and a separate `source-counts.csv`.
  Without a machine-readable table, GEMINI's counts are harder to diff, audit, or
  reuse in the later fine-tuning sprint.
- GEMINI's `Query iNaturalist...` tasks are too generic. They do not require
  observation-level versus photo-level license checks, media-backed filtering, or
  a clear exclusion of NC/ND/unknown licenses. My `Phase 2 - Source Method And
  License Rules` is stronger here.
- GEMINI under-specifies the Pilea side. `Query iNaturalist for hard examples of
  epipremnum-aureum (pothos) and Pilea` does not require verifying the production
  Pilea class, while my `Record search terms for the Pilea side... using the
  exact production model class` prevents generic Pilea counts from polluting the
  decision.
- GEMINI's `Verify that a disjoint train/val/test split is achievable without
  using the 8 existing fixture images` is directionally right but too thin. It
  does not require listing the fixture filenames or excluding the source URL of
  `epipremnum-aureum.jpg`, which matters because pothos is itself part of the
  boundary assessment.
- GEMINI's camera task `Implement the fix to re-enable the shutter button upon
  return to the camera screen` does not constrain the reset behavior around
  `Success`, `Failure`, `Capturing`, or `Identifying`. My `Add a narrow ViewModel
  or navigation-return hook... while preserving Failure retry behavior and
  in-flight capture protection` is safer.
- GEMINI does not require `ktlintCheck`, `testDebugUnitTest`, or focused existing
  camera bind/failure tests by name. It only requires a regression UI test and
  broad networking/stub gates, which is not enough for the one code change in the
  sprint.

### Missing tasks

- Missing a source-count table equivalent to my `Create
  docs/sprints/evidence/PLANTPOTTING-0008/source-counts.csv`.
- Missing a query log equivalent to my `Create
  docs/sprints/evidence/PLANTPOTTING-0008/query-log.md`.
- Missing an evidence-folder `.gitignore` task to prevent accidental image/cache
  commits.
- Missing explicit source-specific tasks for `GBIF occurrence/media records`,
  `Wikimedia Commons categories/search`, and `Flickr-CC search`; GEMINI asks to
  repeat queries but not how to judge media license/provenance quality per source.
- Missing cultivar-proven counting as a hard requirement for
  `philodendron-pink-princess`; it notes cultivar availability but does not say
  generic `Philodendron erubescens` must be excluded from the recommendation.
- Missing hard-example subset estimates for both pothos and Pilea. My tasks
  `For pothos, estimate the subset...` and `For Pilea, estimate the subset...`
  are more specific.
- Missing explicit verification that no research API script is invoked from a
  Gradle app task, instrumentation test, runtime path, or on-device code.
- Missing named camera regression checks:
  `CameraScreenTest.failureRetryReturnsToIdle`,
  `CameraViewModelTest.newCaptureClearsFailureState`,
  `CameraScreenBindStateTest.shutterIsDisabledAndBindProgressVisibleWhileImageCaptureIsNull`,
  and `CameraScreenBoundStateTest.shutterIsEnabledAndBindProgressGoneWhenImageCaptureIsBound`.

### Risks underweighted

- License inflation is underweighted. GEMINI records count/license types, but it
  does not force NC/ND/unknown counts to be separated from threshold-eligible
  counts.
- Cross-source duplicate risk is underweighted. GEMINI mentions multiple sources
  but does not require de-duping GBIF records that mirror iNaturalist media.
- Taxonomic ambiguity is underweighted. It gives one example,
  `Pothos vs Scindapsus/Philodendron lookalikes`, but does not enumerate
  `monstera-adansonii` versus `monstera-deliciosa`, true `Pothos` genus versus
  `Epipremnum aureum`, or generic `Pilea` versus the production class.
- Attribution feasibility is underweighted. GEMINI asks whether provenance
  blocks can be generated, but not whether author, source page, original file or
  observation URL, retrieved date, modifications, license, and attribution string
  are available per image.
- Camera regression risk is underweighted. There is no guard against resetting
  during `Capturing` or `Identifying`, and no explicit preservation of failure
  retry behavior.

### Sequencing that is wrong

- GEMINI says `Phase 1 (Data Assessment) must be completed first`, while also
  saying `Phase 3 (UI Bug Fix) is independent and can be picked up at any time`.
  The latter is correct; the former should be narrowed to "Phase 1 before Phase
  2." The camera fix can run in parallel.
- GEMINI starts count collection before any explicit methodology/scaffold phase.
  My `Phase 1 - Research Artifact Skeleton`, `Phase 2 - Source Method And
  License Rules`, and `Phase 3 - Species And Taxonomy Setup` are better ordered:
  source queries should not begin until license-clean definitions, query logging,
  controlled names, and fixture exclusions exist.
- GEMINI puts the future fine-tuning outline inside
  `docs/sprints/results/PLANTPOTTING-0008.md`. That may be acceptable for a short
  sprint, but a separate handoff artifact like my
  `future-finetuning-outline.md` is cleaner because the results doc should remain
  a summary and gate ledger.

## CLAUDE draft

### What is stronger than mine

- CLAUDE's opening context is stronger. It ties the spike directly to
  PLANTPOTTING-0007, the production `house_plant_species_mobilenetv2` model, 47
  classes, Apache-2.0, float16 TFLite, 10-of-16 KB coverage, and the previous
  AIY comparison. My draft states the production model but does not explain why
  this spike is the next lever.
- CLAUDE's `Target species (the whole assessment surface)` section is clearer
  than my goals list. It separates the six OOV species from the pothos/Pilea
  boundary and explicitly says the boundary is not a new class.
- CLAUDE's `Sources to assess (license-clean only)` is stronger on source
  profiles. The iNaturalist task explicitly warns that `CC-BY-NC is unusable for
  a commercial app model`, which is a sharper framing than my broader accepted
  and excluded license list.
- CLAUDE's `Phase 0 - Scaffolding & inputs (no networking)` improves on my
  setup by requiring extraction of canonical KB species names and aliases from
  `species.json` into `species-targets.md`. My draft records accepted names and
  synonyms, but it does not require reconciling them with the KB source of truth.
- CLAUDE is better on fixture exclusion detail. Its `Record the disjoint-split
  exclusion set` task requires source URLs from `LICENSE.txt` and specifically
  notes that `epipremnum-aureum.jpg` overlaps the boundary target and must be
  excluded from pothos training counts. My draft lists fixture filenames but does
  not explicitly call out the pothos source URL.
- CLAUDE's camera section is much stronger. `Root cause (confirmed in
  CameraScreen.kt:191)` names the stale `CameraUiState.Success(speciesId)` state,
  the back-stack `ViewModelStore`, and the shutter enabled predicate. My draft
  only asks the implementer to inspect these files and find whether stale state
  is the cause.
- CLAUDE's `Phase 4 - Gates (cheap -> expensive)` has better test sequencing:
  `ktlintCheck`, `testDebugUnitTest`, `verifyNoNetworking`,
  `scripts/check-stub-isolation.sh`, instrumented camera tests, and
  `scripts/integration-flow.ps1` cold/warm/buildonly. My draft has the key gates
  but does not include the integration-flow check.
- CLAUDE's `R9 - Counts are a moving target` is useful. My draft records query
  dates in `query-log.md`, but CLAUDE explicitly requires stamping the retrieval
  date and treating counts as a snapshot.
- CLAUDE's `R10 - Windows-portability of query tooling` is stronger for this
  repo context. My draft does not mention Windows portability even though the
  workspace is Windows.

### What is weaker

- CLAUDE has fewer machine-readable evidence requirements than my draft. It has
  `data-availability-report.md` and `go-no-go-matrix.md`, but no CSV equivalent
  to my `source-counts.csv` with one row per species/source/query/license bucket.
  That makes later auditing and automated comparison weaker.
- CLAUDE's source count tasks are less granular than mine. My `Phase 4 -
  Per-Source Count Collection` names each species/source pair and forces the
  relevant pitfall for each row. CLAUDE compresses this into generic source tasks
  plus pitfall audit tasks, which is more readable but easier to under-fill.
- CLAUDE says optional manual eyeball checks can be `<=~20 images` per species,
  but does not require documenting those checks with the same rigor as my
  `query-log.md` and `manual eyeball-check notes` columns. If manual label
  quality affects a GO/NO-GO decision, the notes need to be auditable.
- CLAUDE's `Any other obvious CC/CC0 source` examples include `Plant.id open
  sets`. That may be useful as a search prompt, but the draft should be more
  cautious: only queryable, stable, per-image license metadata should count. My
  `Add a row for "other obvious CC/CC0 source" only when...` task is safer.
- CLAUDE's `Phase 2's hand-off is conditional... write the outline OR the
  fallback memo, not both fully` is efficient, but my draft is better for a
  mixed verdict. A partial-GO sprint may need both a scoped GO plan for species
  that clear the floor and a fallback for blocking species.
- CLAUDE's camera preferred fix says reset on `Success`/`Failure` in one sentence
  and then says decide explicitly whether returning on `Failure` should reset.
  My wording is more conservative about preserving `Failure` retry behavior.

### Missing tasks

- Missing `source-counts.csv` as a diffable row-per-source, row-per-license-bucket
  artifact.
- Missing a dedicated `query-log.md` with exact UI filters, API URLs, dates,
  source quirks, and manual eyeball-check notes. CLAUDE's `methodology.md`
  covers some of this, but methodology and query log serve different purposes.
- Missing a local `.gitignore` under the evidence directory for accidental
  `*.jpg`, `*.jpeg`, `*.png`, `*.webp`, `downloads/`, `samples/`, `raw/`, and
  API cache files.
- Missing explicit per-source license mechanics for iNaturalist photo-level
  versus observation-level license checks. CLAUDE has the right NC warning, but
  my `Add source-specific counting notes for iNaturalist... explain whether
  observation-level and photo-level licenses both need checking` is more precise.
- Missing explicit GBIF media-record filtering detail equivalent to my `avoid
  counting records without image media or usable media licenses`.
- Missing explicit per-image versus aggregate license confidence downgrading,
  covered in my `Record whether each source's license metadata is available per
  image or only at a weaker aggregate level`.
- Missing my acceptance criterion that the report states whether every usable
  source can support provenance blocks matching
  `app/src/androidTest/assets/identify-fixtures/LICENSE.txt`.

### Risks underweighted

- Machine-readability/auditability is underweighted. CLAUDE's `go-no-go-matrix.md`
  is good for synthesis, but without `source-counts.csv`, it is harder to verify
  how each source contributed to the final number.
- Evidence-folder hygiene is underweighted. CLAUDE checks `git status` for no new
  images, but the sprint should also prevent accidental media/cache files locally
  with a targeted `.gitignore`.
- Per-source attribution automation is underweighted. CLAUDE records whether
  author/license/permalink are API-exposed, but does not force a structured
  provenance-field inventory per source row.
- Partial-GO handling is underweighted. CLAUDE supports `NO-GO or partial-GO` in
  the overall verdict, but the conditional handoff instruction prefers outline
  OR fallback. A realistic result may require both: fine-tune sourceable species
  and self-shoot or buy data for `philodendron-pink-princess`.
- The risk that source-specific headline counts include unusable records is
  still slightly underweighted for Commons/Flickr. CLAUDE is excellent on iNat
  NC inflation and GBIF/iNat overlap, but less explicit than my draft about
  Commons file-license mapping and Flickr per-photo attribution verification.

### Sequencing that is wrong

- CLAUDE mostly sequences the sprint correctly. The strongest correction is that
  `Phase 2's hand-off is conditional... write the outline OR the fallback memo`
  should not be exclusive when the gate is partial. The sequence should support
  a GO path for sourceable targets and a fallback path for blocking targets in
  the same outline.
- CLAUDE's `Phase 4 - Gates` puts `ktlintCheck` before `testDebugUnitTest`. That
  is fine for cheap-to-expensive gating, but for debugging the camera fix I would
  usually run the focused `CameraViewModelTest` first, then ktlint once behavior
  is green. My draft's focused-camera-first verification is more useful while
  developing; CLAUDE's ordering is better for final closeout.
- CLAUDE's `Phase 0` says no networking, but its methodology note records exact
  source queries. That should be split: Phase 0 can define query templates and
  thresholds without networking; exact executed URLs and retrieval dates belong
  after Phase 1 count collection.

## If I were merging

- I would keep GEMINI's concise top-level structure, especially the compact
  `Phase 1 / Phase 2 / Phase 3` readability and the explicit rate-limit risk.
- I would keep CLAUDE's PLANTPOTTING-0007 context, `Target species` framing,
  `Phase 0` KB-name/exclusion setup, pothos fixture source-URL exclusion, camera
  root-cause section, retrieval-date risk, Windows-portability risk, and
  integration-flow gate.
- I would keep my CODEX draft's `source-counts.csv`, `query-log.md`,
  evidence-folder `.gitignore`, source-specific license/provenance mechanics,
  cultivar-proven Pink Princess counting, per-side hard-example estimates for
  pothos/Pilea, and named camera regression checks.
