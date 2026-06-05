# PLANTPOTTING-0009 Feedback

**Review date:** 2026-06-05 · **Reviewer:** principal (whichrobevans@gmail.com) · **Outcome:** clean — no bugs, no UX issues.

Text-only KB-expansion sprint (zero ML). Reviewed against the §7 acceptance criteria. All automated
gates green; content vetted; on-device spot-check via a freshly built **v0.3.0** debug APK.

## Bugs

None.

## UX Issues

None.

## Acceptance verification

**Automated (all GREEN):**
- `./gradlew :app:testDebugUnitTest` — BUILD SUCCESSFUL. Includes the bumped count assertions
  (`bundlesExactlyThirtyTwoSpecies` = 32, archetypes = 9, `KbLoaderTest` 32/9) and the new
  `HousePlantClassMapValidationTest` (verbatim-key guard, exactly-26 mapped, Pilea-absence guard,
  existing-10 regression guard).
- `verifyNoNetworking` — GREEN (same run).
- `scripts/check-stub-isolation.sh` — `stub isolation OK`.
- Scope audit (`git diff --stat` planning^..exec): only the 4 KB/ML assets, the touched test files,
  and docs changed. The only `src/main` edits are the 4 JSON assets — every `.kt` change is under
  `src/test`. The production `PlantIdentifier` / `IdentificationResult` seam and the original 16 KB
  species entries are untouched.

**Content vet (accurate, accepted):**
- Toxicity warnings present and mechanism-specific for every toxic species: Dieffenbachia
  (insoluble calcium-oxalate raphides + proteolytic enzymes, "dumb cane" effect, gloves), English
  Ivy (triterpenoid saponins + contact dermatitis), Poinsettia (Euphorbia latex irritant, with the
  accurate "toxicity widely overstated" note), Alocasia/Anthurium/Aglaonema/Schefflera (calcium
  oxalate), Kalanchoe (bufadienolide cardiac glycosides), Dracaena (saponins), Tradescantia (sap
  dermatitis). Non-toxic species correctly flagged safe (Maranta, Boston Fern, Pachira, Areca).
- Venus Flytrap (`carnivorous-peat-sand`) card carries the full special-case mandate:
  distilled/RO/rain water only, **no** fertiliser, **no** lime/dolomite, permanently-damp peat/sand,
  winter dormancy.
- Docs: `docs/kb/ml-mapping-notes.md` §PLANTPOTTING-0009 records the 16 mappings, the coarse/genus
  rows, the Pilea deferral (CI-enforced), the AIY 5/34 live-vocab surprise, and the unprobed-
  calibration known gap. `docs/sprints/evidence/PLANTPOTTING-0009/EVIDENCE.md` records final counts,
  commands, and the editorial-not-calibrated record.

**On-device check:**
- Bumped app version `0.2.0 → 0.3.0` (versionCode 2 → 3) and built a debug APK so the new species
  cards could be exercised on a physical device. APK delivered to the user's Dropbox
  (`plant-potting-0.3.0-debug.apk`). New species cards spot-checked on-device; no issues raised.

## Missing Features

None for this sprint's scope. The 16 new mappings are **editorial / model-vocabulary coverage only**
— intentionally **unprobed** (not calibrated against real photos). Recorded as a known gap, not a
defect (see §8 of the plan and the ROADMAP).

## Notes for Next Sprint

- **Unprobed calibration** of the 16 newly mapped classes is the standing follow-up — needs imagery /
  real-photo probing, out of scope until imagery work is greenlit.
- **Pilea (`Chinese Money Plant`) is deliberately unmapped** (CI-enforced). The next natural sprint
  bundles the Pilea KB entry with the pothos↔Pilea boundary fix / strict confidence gating so a
  confidently-wrong card cannot surface.
- **Coverage now 26 of 47** model classes mapped — 21 remain (incl. the deferred Pilea). A further
  text-only expansion over the remaining popular slice is still cheap and available.
- **Version bump shipped during review** (`0.3.0`) — folded into the review PR rather than a separate
  sprint, at the user's request, to produce a testable build.
