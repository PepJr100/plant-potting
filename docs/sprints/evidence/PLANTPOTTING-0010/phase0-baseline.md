# PLANTPOTTING-0010 — Phase 0 baseline

- Date: 2026-06-05
- Branch: exec/PLANTPOTTING-0010

## Pre-edit gate status (GREEN)
- `./gradlew :app:testDebugUnitTest` — BUILD SUCCESSFUL
- `./gradlew verifyNoNetworking` — BUILD SUCCESSFUL
- `bash scripts/check-stub-isolation.sh` — stub isolation OK

## Baseline debug APK size (anchor for reference-image delta)
- `app/build/outputs/apk/debug/app-debug.apk` = **43,479,722 bytes** (~41.46 MiB)
- Budget for reference-image growth: <= 3-5 MiB over this baseline.

## Reference-image sourcing note (D4)
- CC0/PD-only constraint; 0008 found ~1-6.8% of houseplant imagery is license-clean.
- Sourcing tracked into Phase 6; sprint ships popular subset + placeholder vector for the rest (do NOT block on full coverage).
