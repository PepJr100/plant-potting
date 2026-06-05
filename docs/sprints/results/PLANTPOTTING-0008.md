# PLANTPOTTING-0008 — Results

**Sprint:** Training-data availability spike (gates fine-tuning) + shutter-on-return UX fix
**Executor:** opus (claude-opus-4-8) — this Claude Code session
**Date:** 2026-06-05
**Status:** DONE — all checkboxes `[x]`, all gates GREEN.

## Headline verdict — PARTIAL-GO

A fine-tuning sprint for the production `house_plant_species_mobilenetv2` model **is viable** and
worth committing, scoped per species. Full detail in
`docs/sprints/evidence/PLANTPOTTING-0008/go-no-go-matrix.md`.

> **No training data or model assets were committed.** This was an assess-only desk-research spike:
> counts were gathered via the sources' own read-only count APIs/UI; no images were downloaded into
> the repo or APK, no model was trained, converted, or swapped, and `ACTIVE_MODEL_ROOT` is unchanged.

### Per-species count summary (usable = license-clean CC0/CC-BY/CC-BY-SA, as of 2026-06-05)

| KB target | Usable (deduped est.) | Verdict |
|---|---:|:--:|
| `chlorophytum-comosum` | ~790 | **GO** |
| `philodendron-hederaceum` | ~467 | **GO** (label-QA for pothos cross-tags) |
| `hoya-carnosa` | ~430 | **GO** (cap cultivar-form share) |
| `monstera-adansonii` | ~369 | **GO** (discount deliciosa/obliqua mislabels) |
| `ficus-lyrata` | ~257 | **GO** ⚠ captive-inclusive (research-grade only = 8) |
| `epipremnum-aureum` (pothos, boundary) | ~1470 | **GO** |
| `pilea-peperomioides` (boundary pair) | ~76 | **CONDITIONAL** — below the 150–300 boundary target; supplement |
| `philodendron-pink-princess` (cultivar) | ~5–15 | **NO-GO** — self-shot fallback |

**Key methodological finding (drove the verdicts):** iNaturalist's `quality_grade=research` /
`verifiable=true` filters **exclude captive/cultivated** observations — i.e. almost all houseplant
photos. The relevant trainable pool for a houseplant classifier is **captive-inclusive,
photo-license-clean**. With that correction, *Ficus lyrata* goes 8 → 165 usable and *Pilea* 0 → 33.
**R1 confirmed:** only **1.0–6.8%** of iNat houseplant imagery is license-clean (CC-BY-NC dominates).

### Chosen hand-off

`docs/sprints/evidence/PLANTPOTTING-0008/finetune-sprint-outline.md` — transfer-learn the existing
MobileNetV2 (47 → 52 classes for the 5 GO OOV species) + a boundary hard-example pass on the existing
pothos & Pilea classes, reusing `ModelSwapEvaluationTest` + the `ACTIVE_MODEL_ROOT` switch + the
`identify-fixtures/` provenance pattern. `philodendron-pink-princess` routes to a **self-shot
fallback** (~150–200 first-party images; no cultivar-specific paid dataset exists). The Pilea side of
the boundary (~76 usable) is supplemented with augmentation + ~75–150 self-shot hard examples.

## UX fix status — DONE

**Shutter-on-return bug fixed.** Root cause: a capture advances `CameraViewModel` to terminal
`Success`, and that VM survives in the back-stack-entry `ViewModelStore`; a `Success` only navigates
to the result screen (no pop), so system-back returned to a camera still in `Success` with the
shutter disabled. **Fix:** a `DisposableEffect` lifecycle observer in `CameraScreen.kt` resets
terminal `Success` → `Idle` on `ON_RESUME`. `Failure` is deliberately **not** reset (the banner owns
its retry); `Capturing`/`Identifying` are non-terminal and never match.

- `CameraShutterOnReturnTest.shutterReEnablesAfterReturningToCameraFromResult` (instrumented; capture
  → result → system-back → camera; RED before, GREEN after) — **GREEN on GMD**.
- `CameraViewModelTest.resetFromTerminalSuccessReturnsToIdle` (JVM) — **GREEN**.
- Seam (`PlantIdentifier`/`IdentificationResult`) and nav contract untouched.

## Gate results — all GREEN

| Gate | Result |
|---|---|
| `ktlintCheck` | ✅ GREEN |
| `testDebugUnitTest` (incl. new VM case) | ✅ GREEN |
| `verifyNoNetworking` | ✅ GREEN |
| `scripts/check-stub-isolation.sh` | ✅ GREEN ("stub isolation OK") |
| Instrumented camera tests on `pixel6Api34` GMD | ✅ **6/6 GREEN** (shutter-on-return + bind/bound/smoke/preview) |
| `scripts/integration-flow.ps1 -BuildOnly` | ✅ GREEN (manifest diff passed; model asset present) |
| `scripts/integration-flow.ps1` device-aware (warm) | ✅ GREEN (full flow shutter→low-confidence→recommendation 'aroid chunky', 5 rows) |
| `git status` no new binaries / no app-ref to evidence dir | ✅ verified (evidence dir text-only) |

**Documented note (accepted, not a regression):** the *first* cold device-aware
`integration-flow.ps1` attempt timed out waiting for the camera node while the freshly-booted
swiftshader emulator was still settling ("Retrying to obtain clipboard"). It passed cleanly on the
warm retry once the emulator settled; the same UI flow is independently covered GREEN by the GMD
instrumented suite.

## Constraints preserved

No KB edits; no model/bundle change; `ACTIVE_MODEL_ROOT` unchanged
(`ml/house_plant_species_mobilenetv2`); no new fixtures; no committed image/binary data; the
`PlantIdentifier`/`IdentificationResult` seam is frozen. The only code diff is `CameraScreen.kt`
(+ `CameraViewModelTest`, `CameraShutterOnReturnTest`).

## Evidence package

`docs/sprints/evidence/PLANTPOTTING-0008/`: `README.md`, `species-targets.md`, `methodology.md`,
`source-counts.csv` (38 rows), `query-log.md` (exact probe URLs + dates), `data-availability-report.md`,
`go-no-go-matrix.md`, `finetune-sprint-outline.md`.
