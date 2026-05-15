#requires -version 5.1
# Integration flow for PLANTPOTTING-0001 (and onward sprints).
#
# === Modes (PLANTPOTTING-0002 §3 — Bug 3 fix) ===
# Default (device-aware):
#   - Resolves `adb` from PATH, then $env:ANDROID_HOME, then $env:ANDROID_SDK_ROOT.
#   - Hard-fails if `adb` cannot be resolved, no device is attached, the APK
#     install fails, or the recommendation-screen `ui-hierarchy.xml` cannot
#     be produced.
#   - Installs the debug APK, grants `android.permission.CAMERA`, launches
#     `MainActivity`, drives the §2.1 flow (shutter → See potting mix) by
#     dumping the UI hierarchy and tapping the bounds of the relevant
#     `resource-id` nodes.
#   - Diffs the generated manifest against
#     `docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt`.
#
# `-BuildOnly`:
#   - Skips every device-side step and writes a build-only manifest, diffed
#     against `docs/sprints/expected-artifacts/PLANTPOTTING-0001-buildonly.txt`.
#
# === PLANTPOTTING-0003 §4.8 / §7 fix (Bug A) ===
# - `Invoke-AdbDump` and `Wait-ForNode` live in `scripts/integration-flow-helpers.ps1`
#   so the test harness at `scripts/tests/integration-flow-tests.ps1` can exercise
#   the retry policy against deterministic shims.
# - `Invoke-AdbDump` returns `$false` on the documented `null root node returned by
#   UiTestAutomationBridge` race (and on other soft failures) so `Wait-ForNode`'s
#   retry loop can actually retry. Hard failures (adb missing, transport error)
#   still throw.
# - Cold-launch settle: a 2 s sleep after `am start` before the first dump.
# - `Wait-ForNode` default `maxAttempts` bumped from 8 to 12.
[CmdletBinding()]
param([switch]$BuildOnly)

Set-StrictMode -Version Latest
# `Continue` (not `Stop`) so that benign stderr from native commands
# (`gradlew.bat`, `adb`) does not trip ErrorAction. Failure is detected
# via explicit `$LASTEXITCODE` checks and `throw` statements below.
$ErrorActionPreference = "Continue"

# Dot-source the helper module. This is the same file the test harness uses,
# so any change here is exercised by `scripts/tests/integration-flow-tests.ps1`.
. "$PSScriptRoot/integration-flow-helpers.ps1"

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
$hasModelAsset = $entryList.Contains("assets/ml/aiy_plants_v1/model.tflite")
Write-Host ("       archetypes asset: {0}" -f $hasArchetypesAsset)
Write-Host ("       species asset:    {0}" -f $hasSpeciesAsset)
Write-Host ("       model asset:      {0}" -f $hasModelAsset)

# Step 3: device-aware flow OR explicit -BuildOnly skip.
$deviceScreenshotCount = 0
$archetypeName = $null
$recipeRowCount = 0
$sourceBadge = $null

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

    # PLANTPOTTING-0003 §4.8: cold-launch settle. 8 * 750 ms = 6 s budget was
    # too tight for the AOSP image's foreground transition; sleep 2 s before
    # the first dump so the activity has gained accessibility focus.
    Start-Sleep -Seconds 2

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

    # Extract the source-driven badge text from the result-screen dump.
    $resultDoc = New-Object System.Xml.XmlDocument
    $resultDoc.Load((Resolve-Path $resultDump))
    $badgeNode = $resultDoc.SelectSingleNode("//node[@resource-id='result.sourceBadge']")
    if ($null -ne $badgeNode) {
        $sourceBadge = $badgeNode.GetAttribute("text").ToLower()
        Write-Host ("       source-badge = '{0}'" -f $sourceBadge)
    } else {
        Write-Host "       warning: result.sourceBadge node missing in dump" -ForegroundColor Yellow
    }

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
[void]$lines.Add("model-asset-present=" + $hasModelAsset.ToString().ToLower())
[void]$lines.Add("verify-no-networking-passed=true")
if ($BuildOnly) {
    [void]$lines.Add("manifest-mode=build-only")
} else {
    [void]$lines.Add("manifest-mode=device-aware")
    $hasScreenshot = ($deviceScreenshotCount -ge 1)
    [void]$lines.Add("device-screenshot-count-at-least-1=" + $hasScreenshot.ToString().ToLower())
    [void]$lines.Add("archetype-name=$archetypeName")
    [void]$lines.Add("recipe-row-count=$recipeRowCount")
    if ($null -ne $sourceBadge -and $sourceBadge -ne "") {
        [void]$lines.Add("source-badge=$sourceBadge")
    }
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
