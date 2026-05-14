#requires -version 5.1
# Integration flow for PLANTPOTTING-0001 (and onward sprints).
#
# === Modes (PLANTPOTTING-0002 §3 — Bug 3 fix) ===
# Default (device-aware):
#   - Resolves `adb` from PATH, then $env:ANDROID_HOME, then $env:ANDROID_SDK_ROOT.
#   - Hard-fails if `adb` cannot be resolved, no device is attached, the APK
#     install fails, or the recommendation-screen `ui-hierarchy.xml` cannot
#     be produced. Bug 3 was that the previous script silently skipped all of
#     this when `adb` was not on PATH — never again.
#   - Installs the debug APK, grants `android.permission.CAMERA`, launches
#     `MainActivity`, drives the §2.1 flow (shutter → See potting mix) by
#     dumping the UI hierarchy and tapping the bounds of the relevant
#     `resource-id` nodes (Compose `testTag`s are surfaced as resource ids
#     via §3.6's `Modifier.semantics { testTagsAsResourceId = true }`).
#   - Diffs the generated manifest against
#     `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`.
#
# `-BuildOnly`:
#   - Skips every device-side step and writes a build-only manifest, diffed
#     against `docs/sprints/expected-artifacts/PLANTPOTTING-0001-buildonly.txt`.
#   - For CI machines or contributors with no device attached. A
#     build-only run is explicitly NOT acceptance for any device-aware
#     gate (per §3.1 manifest-mode policy).
#
# === Manual repro for Bug 3 (PLANTPOTTING-0002 §3.10) ===
# 1. `adb` removed from PATH, `$env:ANDROID_HOME` set, device attached:
#      Remove-Item Env:\PATH -ErrorAction Ignore
#      $env:PATH = ($oldPath -split ';' | Where-Object { $_ -notmatch 'platform-tools' }) -join ';'
#      ./scripts/integration-flow.ps1
#    → Expected: device-aware diff passes via Resolve-AdbPath fallback.
# 2. No device attached, no `-BuildOnly`:
#      ./scripts/integration-flow.ps1
#    → Expected: non-zero exit, "No device attached" message.
# Transcripts of both runs are committed at
# `docs/sprints/evidence/PLANTPOTTING-0002/integration-flow-transcripts.md`.
[CmdletBinding()]
param([switch]$BuildOnly)

Set-StrictMode -Version Latest
# `Continue` (not `Stop`) so that benign stderr from native commands
# (`gradlew.bat`, `adb`) does not trip ErrorAction. Failure is detected
# via explicit `$LASTEXITCODE` checks and `throw` statements below.
$ErrorActionPreference = "Continue"

$pkg = "com.darkfactory.plantpotting"
$sprintId = "PLANTPOTTING-0001"
$artifactsDir = "artifacts/$sprintId"
$manifestPath = "$artifactsDir/manifest.txt"
$apkPath = "app/build/outputs/apk/debug/app-debug.apk"
$expectedManifest = if ($BuildOnly) {
    "docs/sprints/expected-artifacts/$sprintId-buildonly.txt"
} else {
    "docs/sprints/expected-artifacts/$sprintId.txt"
}

function Resolve-AdbPath {
    $cmd = Get-Command adb -ErrorAction SilentlyContinue
    if ($null -ne $cmd) { return $cmd.Source }
    if ($env:ANDROID_HOME) {
        $candidate = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
        if (Test-Path $candidate) { return $candidate }
    }
    if ($env:ANDROID_SDK_ROOT) {
        $candidate = Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe"
        if (Test-Path $candidate) { return $candidate }
    }
    return $null
}

function Get-NodeBounds-Center([System.Xml.XmlNode]$node) {
    # Compose-exported bounds look like `[x1,y1][x2,y2]`.
    $b = $node.GetAttribute("bounds")
    if ($b -match '^\[(\d+),(\d+)\]\[(\d+),(\d+)\]$') {
        $x1 = [int]$matches[1]
        $y1 = [int]$matches[2]
        $x2 = [int]$matches[3]
        $y2 = [int]$matches[4]
        return [pscustomobject]@{
            X = [int](($x1 + $x2) / 2)
            Y = [int](($y1 + $y2) / 2)
        }
    }
    throw "Could not parse bounds attribute: '$b' on $($node.OuterXml)"
}

function Invoke-AdbDump([string]$adb, [string]$path) {
    & $adb shell uiautomator dump /sdcard/ui.xml | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "uiautomator dump failed (exit $LASTEXITCODE)"
    }
    & $adb pull /sdcard/ui.xml $path 2>&1 | Out-Null
    if (-not (Test-Path $path)) {
        throw "adb pull /sdcard/ui.xml -> $path failed"
    }
}

function Wait-ForNode([string]$adb, [string]$resourceId, [string]$dumpPath, [int]$maxAttempts = 8) {
    for ($i = 1; $i -le $maxAttempts; $i++) {
        Invoke-AdbDump -adb $adb -path $dumpPath
        $doc = New-Object System.Xml.XmlDocument
        $doc.Load((Resolve-Path $dumpPath))
        $node = $doc.SelectSingleNode("//node[@resource-id='$resourceId']")
        if ($null -ne $node) { return $node }
        Start-Sleep -Milliseconds 750
    }
    throw "Timed out waiting for node with resource-id='$resourceId' after $maxAttempts dumps"
}

if (-not (Test-Path "./gradlew.bat")) {
    throw "gradlew.bat not found. Run this script from the repository root."
}
if (-not (Test-Path $expectedManifest)) {
    throw "expected manifest missing at $expectedManifest"
}
if (-not (Test-Path $artifactsDir)) {
    New-Item -ItemType Directory -Path $artifactsDir | Out-Null
}

# Step 1: build the debug APK and run the networking guard.
Write-Host "[1/5] assembleDebug + verifyNoNetworking" -ForegroundColor Cyan
& ./gradlew.bat --no-daemon assembleDebug verifyNoNetworking
if ($LASTEXITCODE -ne 0) {
    throw "gradle assembleDebug failed (exit $LASTEXITCODE)"
}
if (-not (Test-Path $apkPath)) {
    throw "APK not found at $apkPath after build"
}

# Step 2: inspect the APK via .NET ZipFile (Expand-Archive rejects .apk).
Write-Host "[2/5] APK content inspection" -ForegroundColor Cyan
Add-Type -AssemblyName System.IO.Compression.FileSystem
$apkFullPath = (Resolve-Path $apkPath).Path
$zip = [System.IO.Compression.ZipFile]::OpenRead($apkFullPath)
$entryList = New-Object System.Collections.ArrayList
foreach ($e in $zip.Entries) { [void]$entryList.Add($e.FullName) }
$zip.Dispose()
Write-Host ("       entries: {0}" -f $entryList.Count)

$hasArchetypesAsset = $entryList.Contains("assets/kb/archetypes.json")
$hasSpeciesAsset = $entryList.Contains("assets/kb/species.json")
Write-Host ("       archetypes asset: {0}" -f $hasArchetypesAsset)
Write-Host ("       species asset:    {0}" -f $hasSpeciesAsset)

# Step 3: device-aware flow OR explicit -BuildOnly skip.
$deviceScreenshotCount = 0
$archetypeName = $null
$recipeRowCount = 0

if ($BuildOnly) {
    Write-Host "[3/5] -BuildOnly mode -- skipping device coverage" -ForegroundColor Yellow
} else {
    Write-Host "[3/5] device-aware flow" -ForegroundColor Cyan
    $adb = Resolve-AdbPath
    if ($null -eq $adb) {
        throw "adb not found on PATH, in `$env:ANDROID_HOME\platform-tools, or in `$env:ANDROID_SDK_ROOT\platform-tools -- refusing to silently skip device coverage (Bug 3). Pass -BuildOnly to opt out of device coverage explicitly."
    }
    Write-Host ("       adb: {0}" -f $adb)

    $adbDevices = & $adb devices
    $devicePresent = $false
    foreach ($line in $adbDevices) {
        if ($line -match "^\S+\s+device$") { $devicePresent = $true; break }
    }
    if (-not $devicePresent) {
        throw "No device attached in device-aware mode -- refusing to write a misleading manifest. Pass -BuildOnly to opt out of device coverage."
    }

    Write-Host "       installing APK"
    $installOutput = & $adb install -r $apkPath 2>&1
    if ($LASTEXITCODE -ne 0 -or ($installOutput -join "`n") -notmatch "Success") {
        throw "adb install failed (exit $LASTEXITCODE):`n$($installOutput -join "`n")"
    }

    & $adb shell pm grant $pkg android.permission.CAMERA 2>$null | Out-Null
    & $adb shell am force-stop $pkg 2>$null | Out-Null
    & $adb shell am start -n "$pkg/.MainActivity" 2>$null | Out-Null

    # Wait for the camera screen (shutter resource-id).
    Write-Host "       waiting for camera screen"
    $shutterDump = "$artifactsDir/ui-hierarchy-camera.xml"
    $shutterNode = Wait-ForNode -adb $adb -resourceId "camera.shutter" -dumpPath $shutterDump

    # Initial screenshot.
    $screenshot1 = "$artifactsDir/01-launch.png"
    & $adb exec-out screencap -p > $screenshot1
    if ((Test-Path $screenshot1) -and (Get-Item $screenshot1).Length -gt 0) {
        $deviceScreenshotCount++
    }

    # Tap the shutter centre.
    $shutterCenter = Get-NodeBounds-Center -node $shutterNode
    Write-Host ("       tapping shutter at ({0},{1})" -f $shutterCenter.X, $shutterCenter.Y)
    & $adb shell input tap $shutterCenter.X $shutterCenter.Y | Out-Null

    # Wait for the result screen ("See potting mix" CTA).
    Write-Host "       waiting for result screen"
    $resultDump = "$artifactsDir/ui-hierarchy-result.xml"
    $seePottingMixNode = Wait-ForNode -adb $adb -resourceId "result.seePottingMix" -dumpPath $resultDump

    $seePottingMixCenter = Get-NodeBounds-Center -node $seePottingMixNode
    Write-Host ("       tapping 'See potting mix' at ({0},{1})" -f $seePottingMixCenter.X, $seePottingMixCenter.Y)
    & $adb shell input tap $seePottingMixCenter.X $seePottingMixCenter.Y | Out-Null

    # Wait for recommendation screen.
    Write-Host "       waiting for recommendation screen"
    $hierarchy = "$artifactsDir/ui-hierarchy.xml"
    $archetypeNode = Wait-ForNode -adb $adb -resourceId "recommendation.archetypeName" -dumpPath $hierarchy

    $screenshot2 = "$artifactsDir/02-recommendation.png"
    & $adb exec-out screencap -p > $screenshot2
    if ((Test-Path $screenshot2) -and (Get-Item $screenshot2).Length -gt 0) {
        $deviceScreenshotCount++
    }

    $doc = New-Object System.Xml.XmlDocument
    $doc.Load((Resolve-Path $hierarchy))
    $archetypeNodeFinal = $doc.SelectSingleNode("//node[@resource-id='recommendation.archetypeName']")
    if ($null -eq $archetypeNodeFinal) {
        throw "recommendation.archetypeName missing in $hierarchy -- script did not reach the recommendation screen"
    }
    $archetypeName = $archetypeNodeFinal.GetAttribute("text").ToLower()
    $rowNodes = $doc.SelectNodes("//node[@resource-id='recommendation.recipeRow']")
    $recipeRowCount = if ($null -ne $rowNodes) { $rowNodes.Count } else { 0 }
    Write-Host ("       archetype-name = '{0}', recipe-row-count = {1}" -f $archetypeName, $recipeRowCount)
}

# Step 4: write the manifest.
Write-Host "[4/5] writing manifest" -ForegroundColor Cyan
$lines = New-Object System.Collections.ArrayList
[void]$lines.Add("apk-exists=true")
[void]$lines.Add("archetypes-asset-present=" + $hasArchetypesAsset.ToString().ToLower())
[void]$lines.Add("species-asset-present=" + $hasSpeciesAsset.ToString().ToLower())
[void]$lines.Add("verify-no-networking-passed=true")
if ($BuildOnly) {
    [void]$lines.Add("manifest-mode=build-only")
} else {
    [void]$lines.Add("manifest-mode=device-aware")
    $hasScreenshot = ($deviceScreenshotCount -ge 1)
    [void]$lines.Add("device-screenshot-count-at-least-1=" + $hasScreenshot.ToString().ToLower())
    [void]$lines.Add("archetype-name=$archetypeName")
    [void]$lines.Add("recipe-row-count=$recipeRowCount")
}
Set-Content -Path $manifestPath -Value ($lines -join "`n") -Encoding utf8 -NoNewline

# Step 5: diff against expected.
Write-Host ("[5/5] diff against {0}" -f $expectedManifest) -ForegroundColor Cyan
$expectedLines = Get-Content $expectedManifest |
    Where-Object { $_ -and (-not $_.TrimStart().StartsWith("#")) } |
    ForEach-Object { $_.Trim().ToLower() }
$actualLines = $lines | ForEach-Object { $_.Trim().ToLower() }

$missing = @($expectedLines | Where-Object { $actualLines -notcontains $_ })
if ($missing.Count -gt 0) {
    Write-Host "Missing expected lines:" -ForegroundColor Red
    foreach ($m in $missing) { Write-Host "  - $m" -ForegroundColor Red }
    exit 1
}
Write-Host "Integration manifest diff passed." -ForegroundColor Green
