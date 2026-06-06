# PLANTPOTTING-0010 Feedback

Review of the app-experience sprint (UI/UX refresh + "Add this plant" wireframe + KB expansion).
Execution already folded in 7 rounds of on-device principal feedback before this formal review
(see [`HANDOFF.md`](HANDOFF.md)). This review re-walked the acceptance criteria on the v0.4.0-r7
debug APK + the 7 screenshots in `Screenshots/`.

**Overall: clean. No bugs.** All automated gates GREEN; all reviewed surfaces "looks good". The
substantive output of the review is one **model-quality** observation (not a 0010 regression — the
model was untouched this sprint) plus three minor refinements, all routed to next-sprint planning.

## Automated gates (all GREEN)

- `./gradlew :app:testDebugUnitTest verifyNoNetworking :app:lintDebug` → **BUILD SUCCESSFUL** (exit 0).
- `scripts/check-stub-isolation.sh` → `stub isolation OK`.
- **Seam audit:** `identify/PlantIdentifier.kt` byte-for-byte unchanged across the entire sprint
  (`git diff afad618..HEAD` empty).
- **Version bump:** `versionCode = 4`, `versionName = "0.4.0"`.
- **Pilea:** appears only in the class-map `_comment` documenting it as deliberately unmapped — no
  mapping entry. CI Pilea-absence guard semantics intact.

## Bugs

None. The app behaves as designed on every reviewed surface.

## UX Issues

### Some reference images are botanical illustrations/plates, not photos
**What happened:** A subset of the bundled CC0/PD reference images are vintage botanical *plates*
(line/colour drawings) rather than photographs — the only license-clean option for those species
(per HANDOFF: peace lily, poinsettia, parlor palm, dracaena, `philodendron-pink-princess`). Next to
the real photos they read as odd/inconsistent.
**Why it matters:** Visual inconsistency on the detail/result views; a drawing where the user expects
a photo undercuts the "real app" feel the sprint was chasing.
**Suggested fix:** Source CC0/PD *photographs* for those species if available, or allow the broader
"free-to-use" (Unsplash/Pexels, non-PD) bucket via the existing
`scripts/source-reference-images.ps1` license filter (a deliberate licensing-policy decision for the
principal). Infra is already in place — drop-in replacement only.
**Impact:** Low — cosmetic, not blocking.

### Confidence bar could be slightly thicker
**What happened:** The `LinearProgressIndicator` confidence bar on `ResultScreen` is visually thin.
**Why it matters:** Pure polish; the user wants it slightly fatter for presence.
**Suggested fix:** Bump the bar height/stroke in `ResultScreen`.
**Impact:** Trivial — visual nit.

## Missing Features

- **My Plants should survive uninstall/reinstall.** Today persistence is app-local DataStore, which
  Android wipes on uninstall. The principal would like saved plants to persist across a reinstall.
  Note this brushes the 0010 non-goal "no cloud sync / no remote telemetry" — the license-clean path
  is **Android Auto Backup** (Backup-to-Google-Drive of the DataStore file, on-device/Google-managed,
  no app server) or a **local export/import** file. A planner decision: which mechanism, and whether
  it re-opens the cloud-sync non-goal. Not in 0010 scope; route to next sprint.

## Notes for Next Sprint

- **★ ML matching accuracy is poor on real-world captures (highest-priority observation).** Principal
  reports a snake plant is correctly identified only ~1 in 3 captures; the other ~2/3 it *confidently*
  predicts a different plant. This is **not a 0010 regression** — 0010 was an app-experience sprint and
  the model/labels were untouched. It is the standing **calibration / model-quality gap** flagged since
  0006/0007/0009: the production House_plant_species MobileNetV2 default plus the 0009/0010 delta
  mappings are **editorial coverage, not calibrated** against real photos. The confident-but-wrong
  failure mode is the worst kind for trust. Candidate next-sprint directions:
  - Calibration probe sweep over the common species (extend the 0006-style in-vocab probes) to quantify
    real-world top-1 accuracy and find a sane high-confidence threshold.
  - Confidence-threshold / abstention tuning so confident-wrong cards become "not sure → pick manually"
    instead of a wrong confident card.
  - Fine-tuning remains **deferred** (0008 PARTIAL-GO: license-clean / self-shot training data is the
    blocker) — re-evaluate only if a data path opens.
  This is the natural headline for the next sprint and likely supersedes the long-deferred pothos↔Pilea
  boundary fix in priority.
- **"Add this plant" not exercised on-device.** The principal didn't manage to trigger a
  confident-but-unmapped class (Pilea is the intended live fixture) during review. The flow is covered
  by unit tests (`CameraViewModelTest` routing: AddPlant vs picker vs Success; `AddThisPlant` VM/screen
  increment test), so this is a **coverage note, not a finding** — worth a deliberate on-device trigger
  next time an emulator/device round happens.
- Theme candidates (LEAF/TERRACOTTA/SLATE) remain in code but unreachable (switcher removed during
  execution; production default LEAF). A non-LEAF default is a one-line change if ever wanted.
