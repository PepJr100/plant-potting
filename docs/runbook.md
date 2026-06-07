# PlantPotting — Runbook

Operational runbook for building, running, and testing PlantPotting on a developer
machine (Windows-first, since that's the developer's box). This complements the
[`README.md`](../README.md), which holds the canonical first-time-setup walkthrough — when
the two disagree, the README wins and this file should be updated. For the sprint pipeline,
see [`docs/buildworkflow.md`](buildworkflow.md).

## Prerequisites

- **JDK 17** (Temurin or any LTS-17). The Gradle wrapper expects Java 17.
- **Android SDK with platform 34.** Set `ANDROID_HOME` (or `ANDROID_SDK_ROOT`).
- For instrumentation tests on the Gradle Managed Device (`pixel6Api34`): host
  virtualization (WHPX on Windows / KVM on Linux) and ~6 GB free disk for the AOSP system
  image.

## First-time setup (Windows) — short form

Full step-by-step (with winget commands and screenshots of where each toggle lives) is in
the [README "First-time setup" section](../README.md#first-time-setup-windows). In brief:

1. **JDK 17** — `winget install EclipseAdoptium.Temurin.17.JDK`; verify `java -version`.
2. **Android Studio / SDK** — `winget install Google.AndroidStudio`; install API 34
   platform + Build-Tools 34 + Command-line Tools + Platform-Tools + Emulator. Set
   `ANDROID_HOME` and put `platform-tools` on `PATH` so `adb` resolves.
3. **Hardware acceleration (WHPX)** — enable `HypervisorPlatform` from an elevated shell
   and reboot if not already on.
4. **Create the AVD** — Pixel 6, **API 34 with Google APIs** (the Google APIs image has a
   synthetic camera; the plain AOSP image used by GMD does not). Boot it once.
5. **First build** — `./gradlew.bat assembleDebug` (first run downloads Gradle 8.9 + the
   AGP chain; 5–10 min).

## Daily commands

Run from the repo root. PowerShell assumed; substitute `./gradlew` for `./gradlew.bat`
under bash.

| Task | Command |
| --- | --- |
| Build debug APK | `./gradlew.bat assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk` |
| JVM unit tests (no emulator) | `./gradlew.bat testDebugUnitTest` |
| Install + launch on booted emulator | `./gradlew.bat installDebug` then `adb shell am start -n com.darkfactory.plantpotting/.MainActivity` |
| Instrumentation vs booted emulator | `./gradlew.bat connectedDebugAndroidTest` |
| Instrumentation via GMD (what CI runs) | `./gradlew.bat pixel6Api34DebugAndroidTest` |
| Lint + style | `./gradlew.bat lint ktlintCheck` |
| Stub-isolation check | `bash scripts/check-stub-isolation.sh` |
| Install on a connected device | `./gradlew.bat installDebug` (or `adb install -r …app-debug.apk`) |

If `adb` isn't on `PATH`, call it directly:
`& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" …`.

## Manual emulator walkthrough

With the emulator booted and the debug build installed:

1. Tap **Grant camera access**.
2. Tap the shutter on the camera preview.
3. Expect **one of two** outcomes — high-confidence → `ResultScreen` (`on-device match`),
   or low-confidence → `LowConfidencePicker` (pick a species; AOSP's synthetic camera
   rarely clears the threshold, so this path is common on emulator). See
   [`docs/userguide.md`](userguide.md) for what each screen means.
4. Tap **See potting mix** → `RecommendationScreen` (archetype + rationale + recipe summing
   to 100%).
5. Tap **Retake** to loop.

## Device-aware end-to-end smoke (recommended health check)

```powershell
pwsh ./scripts/integration-flow.ps1
```

Builds the debug APK (+ `verifyNoNetworking`), confirms the model + KB assets are bundled,
installs to the booted emulator, grants `CAMERA`, drives the full flow by tapping
`uiautomator dump` coordinates (handles high- and low-conf paths), reads back the source
badge and recipe-row count, writes an integration manifest, and diffs it against
`docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`. Success ends with
`Integration manifest diff passed.`

Build-only mode (skips the device — useful offline; diffs against the `*-buildonly.txt`
baseline):

```powershell
pwsh ./scripts/integration-flow.ps1 -BuildOnly
```

## Full pre-PR check

```powershell
./gradlew.bat assembleDebug testDebugUnitTest lint ktlintCheck pixel6Api34DebugAndroidTest verifyNoNetworking
bash scripts/check-stub-isolation.sh
pwsh ./scripts/integration-flow.ps1
pwsh ./scripts/integration-flow.ps1 -BuildOnly
```

Convenience: `pwsh ./scripts/check-android.ps1` runs the gradle gate chain in one go (it
does **not** include the integration-flow scripts).

## Troubleshooting

| Symptom | Fix |
| --- | --- |
| `java -version` ≠ 17 | Set `JAVA_HOME` to the Temurin 17 dir; put `%JAVA_HOME%\bin` first on `PATH`. |
| Gradle can't find the SDK | `setx ANDROID_HOME "$env:LOCALAPPDATA\Android\Sdk"`, open a fresh shell. |
| `adb` not found | Add `…\Android\Sdk\platform-tools` to `PATH`, or call `adb.exe` by full path. |
| Emulator boots slowly / hangs | Confirm WHPX (`Get-WindowsOptionalFeature -Online -FeatureName HypervisorPlatform`); enable + reboot. Leave the emulator running between gradle runs. |
| GMD test downloads stall | First `pixel6Api34DebugAndroidTest` run pulls a ~2 GB AOSP image — expected once. |
| Low-confidence picker always shows on emulator | Expected: AOSP's synthetic camera scene rarely clears the threshold. The active House Plant Species model (PLANTPOTTING-0007) maps 39 of its 47 classes to KB care cards (widened from 10 by 0009/0010/0012); use a real device photo of an in-vocab species (snake plant, ZZ, monstera, jade, …) to hit the high-confidence path. |
| `bash scripts/check-stub-isolation.sh` parse error in PowerShell | Run it on its own statement — don't append it to a `gradlew` line, PowerShell reads `bash …` as a gradle task name. Git Bash required. |
| CLI sub-spawns (`claude`/`gh`) fail from bash | npm `.ps1` shims are broken on this machine — use full paths per `.claude/CLAUDE.md` (`/c/Users/robev/.local/bin/claude.exe`, `/c/Program Files/GitHub CLI/gh.exe`). |
