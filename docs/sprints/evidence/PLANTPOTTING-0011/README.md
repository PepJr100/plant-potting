# PLANTPOTTING-0011 — evidence

Accuracy & trust sprint (MEASURE → IMPROVE → ABSTAIN). This directory holds the
committed evidence artifacts produced by the sprint. The GMD `pixel6Api34`
device test is **CI-only** (no local emulator on the dev box) — the scorecard
numbers below are produced by the GitHub Actions instrumented-test job and pulled
from its `gradle-reports` artifact.

## Artifacts dropped here

| File | Phase | What it is |
|---|---|---|
| `accuracy-eval.csv` | 1c / 3b | Per-row scorecard: every clean fixture + every perturbation, run through the production model under each preprocessing mode. One row per (fixture, perturbation, mode). Columns include raw top-1/top-2 label+score, top1−top2 **margin**, mapped top-1/top-3, route, **`confident_wrong`**, latency. This single rich CSV is the input to the offline preprocessing comparison (Phase 2) and the offline abstention threshold sweep (Phase 3). |
| `accuracy-eval-summary.md` | 1c | Aggregate scorecard: top-1, top-3, confident-wrong rate, abstain rate, median+worst latency — reported separately for **clean** and **each perturbation family**, with an **in-vocab-only** cut and a **per-base-image-averaged** cut. The committed **BEFORE** number (current thresholds + `squash` preprocessing). |
| `fixture-manifest-summary.md` | 1a | Honest scarcity log: every target species that could **not** be sourced cleanly (CC0/PD), plus rejected near-misses where the license wasn't clean enough. No silent skips. |
| `preprocessing-decision.md` | 2 | ADOPT/DROP for center-crop, multi-crop/TTA, orientation — each with top-1 before/after, confident-wrong before/after, median+worst latency, and the number that drove the decision. TTA adopted only if it beats center-crop. |
| `abstention-before-after.md` | 3b | The abstention headline: confident-wrong rate ↓ vs BEFORE, the accepted abstain-rate cost, the chosen setting, rejected candidates, and the **held-out** generalisation number (leave-one-species-out + perturbation rows not tuned against). |

## `adb pull` recipe (mirrors `ModelSwapEvaluationTest` external-files convention)

The instrumented test writes its CSV + summary to the app's external files dir.
After the GMD run (CI) or a local `connectedDebugAndroidTest`, pull them:

```bash
adb shell run-as com.darkfactory.plantpotting ls files/   # confirm names
adb pull /sdcard/Android/data/com.darkfactory.plantpotting/files/accuracy-eval.csv
adb pull /sdcard/Android/data/com.darkfactory.plantpotting/files/accuracy-eval-summary.md
```

In CI the same files are surfaced via the `gradle-reports` / instrumented-test
artifact; download that artifact and copy the two files here, then commit them as
the documented BEFORE (Phase 1c) and AFTER (Phase 3b) numbers. (`adb` is not on
the dev box PATH — see memory `apk_delivery_dropbox`.)

## Pre-sprint baseline (GREEN anchor — recorded 2026-06-06, before any 0011 change)

Every later "GREEN throughout" claim in this sprint anchors here. Captured on the
dev box (Windows) before the first 0011 commit:

| Gate | Command | Result |
|---|---|---|
| Unit suite | `./gradlew :app:testDebugUnitTest` | **GREEN** (`BUILD SUCCESSFUL`, up-to-date) |
| No-networking | `./gradlew verifyNoNetworking` | **GREEN** |
| Stub isolation | `bash scripts/check-stub-isolation.sh` | **GREEN** (`stub isolation OK`) |
| Instrumented compile | `./gradlew :app:compileDebugAndroidTestKotlin` | **GREEN** (`BUILD SUCCESSFUL`) |

Correction: a **local `Pixel_6_API_34` emulator IS available** on the dev box (the earlier
"CI-only / no local emulator" assumption was wrong). The scorecard BEFORE/AFTER numbers this sprint
were produced on that local emulator (boots ~30 s, `connectedDebugAndroidTest` / `adb am instrument`);
CI runs the same GMD task as a backstop.

## Final guard sweep (close — 2026-06-06)

All GREEN at sprint close:

| Gate | Result |
|---|---|
| `./gradlew :app:testDebugUnitTest` (incl. new abstain/manifest/fixture/credits tests) | **GREEN** |
| `verifyNoNetworking` | **GREEN** |
| `bash scripts/check-stub-isolation.sh` | **GREEN** (`stub isolation OK`) |
| `:app:lintDebug` (`abortOnError`) | **GREEN** |
| `:app:compileDebugAndroidTestKotlin` | **GREEN** |
| `AccuracyEvalTest` on local `pixel6Api34` | **GREEN** (scorecard emitted) |
| AIY baseline anchor (`OnDeviceModelRealInterpreterTest`) | **unchanged** |
| Pilea absence (`HousePlantClassMapValidationTest`) | **GREEN** (Pilea unmapped) |
| Fixture + reference license cross-checks | **GREEN** (CC0/PD/CC-BY; no CC-BY-SA) |

Frozen seams (`PlantIdentifier` / `IdentificationResult` / `IdSource`) byte-for-byte unchanged.
v0.5.0 debug APK delivered to the Dropbox APK folder (43.5 MB, verified). On-device principal
spot-check is the only open item.
