# PLANTPOTTING-0010 — Phase 8 delivery & gates

- Date: 2026-06-06
- Branch: exec/PLANTPOTTING-0010
- Version: versionCode 3→4, versionName 0.3.0→0.4.0

## Gate set — all GREEN

| Gate | Result |
|---|---|
| `./gradlew :app:testDebugUnitTest` | BUILD SUCCESSFUL (full suite) |
| `./gradlew verifyNoNetworking` | BUILD SUCCESSFUL (DataStore added, still clean) |
| `bash scripts/check-stub-isolation.sh` | stub isolation OK |
| `./gradlew :app:lintDebug` (abortOnError=true) | BUILD SUCCESSFUL |
| `./gradlew :app:assembleDebug` | BUILD SUCCESSFUL |
| `pixel6Api34DebugAndroidTest` (GMD) | NOT run locally (no emulator in this env; instrumented tests compile clean — `compileDebugAndroidTestKotlin` GREEN). Run during review/CI; known ~1h transient timeout → rerun once. |

## APK size — within budget

| Build | Bytes | ~MiB |
|---|---|---|
| Phase-0 baseline (v0.3.0) | 43,479,722 | 41.46 |
| Phase-8 (v0.4.0) | 43,884,321 | 41.85 |
| **Delta** | **+404,599** | **+0.39 MiB** |

Budget was ≤3–5 MiB. Actual growth ~0.39 MiB, dominated by the `androidx.datastore`
dependency + new feature code. **Reference-image contribution: ~0** — no CC0/PD WebPs are
bundled (placeholder-only this sprint; see `docs/licenses/reference-images.md`). The one
authored placeholder vector is negligible.

## Delivery

- Debug APK copied to `C:\Users\robev\Dropbox\Curser codeing\Plant_potting_APKs\app-debug-PLANTPOTTING-0010-v0.4.0.apk`.
- Verified from PowerShell: landed at 43,884,321 bytes (matches the build output), timestamp 2026-06-06 00:40.

## On-device review still to do (review phase)

- Capture per-candidate theme screenshots → `docs/sprints/evidence/PLANTPOTTING-0010/themes/`
  (flip the debug "Theme" affordance on `CameraScreen` → LEAF / TERRACOTTA / SLATE).
- Exercise My Plants Save + restart round-trip, Add-this-plant on a confident-but-unmapped
  capture (Pilea is the live fixture), confidence %/bar, and contained search on-device.
