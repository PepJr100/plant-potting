#requires -version 5.1
# Integration flow for PLANTPOTTING-0001.
#
# Builds the debug APK, optionally installs it on a connected device,
# captures screenshots, and writes an artifact manifest under
# artifacts/PLANTPOTTING-0001/manifest.txt. The manifest is diffed against
# docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt; the script fails
# if any expected line is missing from the produced manifest.

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$pkg = "com.darkfactory.plantpotting"
$sprintId = "PLANTPOTTING-0001"
$artifactsDir = "artifacts/$sprintId"
$manifestPath = "$artifactsDir/manifest.txt"
$expectedManifest = "docs/sprints/expected-artifacts/$sprintId.txt"
$apkPath = "app/build/outputs/apk/debug/app-debug.apk"

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

# Step 3: optional adb-driven device flow.
Write-Host "[3/5] adb device check" -ForegroundColor Cyan
$adbDevicePresent = $false
$adbPath = (Get-Command adb -ErrorAction SilentlyContinue)
if ($null -ne $adbPath) {
    $adbOutput = & adb devices
    foreach ($line in $adbOutput) {
        if ($line -match "^\S+\s+device$") { $adbDevicePresent = $true }
    }
}

$deviceScreenshotCount = 0
if ($adbDevicePresent) {
    Write-Host "       device present -- installing + driving"
    & adb install -r $apkPath | Out-Null
    & adb shell pm grant $pkg android.permission.CAMERA 2>$null
    & adb shell am start -n "$pkg/.MainActivity" | Out-Null
    Start-Sleep -Seconds 3
    $screenshot1 = "$artifactsDir/01-launch.png"
    & adb exec-out screencap -p > $screenshot1
    if (Test-Path $screenshot1) { $deviceScreenshotCount++ }
    $hierarchy = "$artifactsDir/ui-hierarchy.xml"
    & adb shell uiautomator dump /sdcard/ui.xml | Out-Null
    & adb pull /sdcard/ui.xml $hierarchy | Out-Null
} else {
    Write-Host "       no device -- build-only manifest"
}

# Step 4: write the manifest.
Write-Host "[4/5] writing manifest" -ForegroundColor Cyan
$lines = New-Object System.Collections.ArrayList
[void]$lines.Add("apk-exists=true")
[void]$lines.Add("archetypes-asset-present=" + $hasArchetypesAsset.ToString().ToLower())
[void]$lines.Add("species-asset-present=" + $hasSpeciesAsset.ToString().ToLower())
[void]$lines.Add("verify-no-networking-passed=true")
if ($adbDevicePresent) {
    $hasScreenshot = ($deviceScreenshotCount -ge 1)
    [void]$lines.Add("device-screenshot-count-at-least-1=" + $hasScreenshot.ToString().ToLower())
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
