# PLANTPOTTING-0001 — Results

**Sprint:** PLANTPOTTING-0001
**Executor:** opus (this Claude Code session)
**Status at handoff:** in-progress — code complete; verification gated on the user
running Gradle locally / CI

---

## 1. What was built

All Phase 0 through Phase 7 tasks landed, plus Phase 8 instrumentation
test scaffolding, the integration script, and the expected manifest.
See `docs/sprints/PLANTPOTTING-0001.md` for the per-task checkbox state.

Architecture seams worth pointing at for the next sprint:

- **`identify/PlantIdentifier`** is the single point of replacement for
  the real on-device ML model. Phase 4 §4.1/§4.3 of the sprint plan
  intentionally keeps this surface to one `suspend fun`, one result
  record, one source enum. PLANTPOTTING-000X (the next sprint) replaces
  `StubPlantIdentifier` behind this seam.
- **`identify/IdentifyModule`** is the Hilt binding. The §4.5 grep guard
  (`scripts/check-stub-isolation.sh`) ensures no consumer outside
  `identify/` references the stub class by name.
- **`kb/`** ships the validated knowledge base — 8 archetypes, 16
  species, with citations pointing back to
  `docs/kb/plant-substrate-kb-notes.md`.

## 2. Commands and how to run them

```bash
./gradlew assembleDebug                          # build the debug APK
./gradlew testDebugUnitTest                      # JVM unit tests (Robolectric, Truth, Turbine)
./gradlew lint ktlintCheck                       # static analysis
./gradlew pixel6Api34DebugAndroidTest            # GMD instrumentation tests
./gradlew verifyNoNetworking                     # custom task: fail on networking deps
bash scripts/check-stub-isolation.sh             # §4.5 seam guard
pwsh scripts/integration-flow.ps1                # build + APK manifest + diff
```

Local Windows shortcut: `pwsh scripts/check-android.ps1` runs the same
chain CI runs.

## 3. Verification results

**This implementer session could not execute Gradle.** No JDK 17 or
Android SDK is configured on the implementer's machine, and the Gradle
wrapper would need to download Gradle 8.9 + the AGP toolchain over the
network. The implementer wrote and committed the code, but the user
must run the commands above on a machine with JDK 17 + Android SDK 34
installed to confirm green.

Fill in the rows below after running:

| Command                                    | Result | Notes |
| ------------------------------------------ | ------ | ----- |
| `./gradlew assembleDebug`                  |        |       |
| `./gradlew testDebugUnitTest`              |        |       |
| `./gradlew lint`                           |        |       |
| `./gradlew ktlintCheck`                    |        |       |
| `./gradlew pixel6Api34DebugAndroidTest`    |        |       |
| `./gradlew verifyNoNetworking`             |        |       |
| `bash scripts/check-stub-isolation.sh`     | passed | run locally during implementation |
| `pwsh scripts/integration-flow.ps1`        |        |       |

## 4. Integration manifest diff

After running `pwsh scripts/integration-flow.ps1`, the diff result
against `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`:

- [ ] Clean diff
- [ ] Failing diff (paste output below)

## 5. Real-device evidence

Per §7.5 risk mitigation, the binding acceptance evidence is the Gradle
Managed Device run, not a physical device. The implementer had **no
physical Android device available**, so §8.7 evidence was not captured.

If the user has a device, place screenshots or a screen recording under
`docs/sprints/evidence/PLANTPOTTING-0001/`.

## 6. Known gaps and follow-ups

- **§0.12** marked unchecked — the implementer could not run the
  green-build verification command. Recheck after a local build.
- **§6.7 `CameraScreenSmokeTest`** is deferred (per the §6 de-scope
  order this is one of the safest cuts). The GMD test in §8.2 plus
  `CameraViewModelTest` cover the state-machine and end-to-end paths.
- **§8.2 `EndToEndFlowTest`** uses a reflective `ViewModelProbe` to
  drive `CameraViewModel.onCaptureReady` directly rather than relying
  on the GMD's camera sensor (AOSP system images don't supply one).
  This works but is fragile; a future sprint should add a proper test
  hook on the activity / a `CameraTestController` injected via Hilt.
- **§8.3 `PermissionDeniedFlowTest`** can verify the permission screen
  is shown at startup but cannot reach the "don't ask again" state
  through pure automation. A manual deny-twice cycle on a real device
  is the only way to fully exercise the permanently-denied UX; this
  remains a known gap for v1.
- **CI workflow** assumes `gradle/actions/setup-gradle@v3` and KVM
  acceleration for the managed device. Real CI run time depends on
  whether the AOSP image cache is warm.

## 7. Handoff to PLANTPOTTING-000X

The real on-device ML model lands behind the `PlantIdentifier`
interface in `app/src/main/java/com/darkfactory/plantpotting/identify/`.
Add a new `OnDevicePlantIdentifier` that implements the same interface,
update `IdentifyModule` to bind it (gated by build flavour or feature
flag), and the rest of the app remains unchanged.
