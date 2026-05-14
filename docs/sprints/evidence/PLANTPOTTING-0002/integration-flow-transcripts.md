# PLANTPOTTING-0002 §3.10 — integration-flow.ps1 manual repro transcripts

Two transcripts capture the device-aware failure modes that Bug 3 used to
silently skip.

## Transcript 1 — adb unresolvable; device-aware mode hard-fails

Reproduces the "no adb on PATH and ANDROID_HOME unset" branch.

```powershell
PS> $env:PATH = ($env:PATH -split ';' | Where-Object { $_ -notmatch 'platform-tools' }) -join ';'
PS> $env:ANDROID_HOME = $null
PS> $env:ANDROID_SDK_ROOT = $null
PS> ./scripts/integration-flow.ps1
[1/5] assembleDebug + verifyNoNetworking
[2/5] APK content inspection
       entries: 558
       archetypes asset: True
       species asset:    True
[3/5] device-aware flow
adb not found on PATH, in $env:ANDROID_HOME\platform-tools, or in $env:ANDROID_SDK_ROOT\platform-tools -- refusing to silently skip device coverage (Bug 3). Pass -BuildOnly to opt out of device coverage explicitly.
At D:\DarkFactoryProject\Plant potting\scripts\integration-flow.ps1:155 char:9
+         throw "adb not found on PATH, in `$env:ANDROID_HOME\platform- ...
+         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : OperationStopped: ...
    + FullyQualifiedErrorId : adb not found on PATH ...
$LASTEXITCODE = 1
```

Captured on 2026-05-14 against the PLANTPOTTING-0002 Phase 3 branch.

## Transcript 2 — `-BuildOnly` succeeds without a device

Documents the explicit opt-out path that contributors with no device use.

```powershell
PS> ./scripts/integration-flow.ps1 -BuildOnly
[1/5] assembleDebug + verifyNoNetworking
[2/5] APK content inspection
       entries: 558
       archetypes asset: True
       species asset:    True
[3/5] -BuildOnly mode -- skipping device coverage
[4/5] writing manifest
[5/5] diff against docs/sprints/expected-artifacts/PLANTPOTTING-0001-buildonly.txt
Integration manifest diff passed.
```

Generated manifest matches the build-only expected file (5 lines including
`manifest-mode=build-only`).

## Transcript 3 — device-aware happy path (pending)

Requires a connected device (real phone or running emulator with virtual
camera scene). Run on the user's machine once a device is available:

```powershell
PS> adb devices  # confirm a `device` status row exists
PS> ./scripts/integration-flow.ps1
```

Expected manifest extras: `archetype-name=aroid chunky`, `recipe-row-count=5`,
`device-screenshot-count-at-least-1=true`, `manifest-mode=device-aware`.
