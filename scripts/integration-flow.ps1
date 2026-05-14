#requires -version 5.1
<#
.SYNOPSIS
Integration flow for PLANTPOTTING-0001.

.DESCRIPTION
Builds the debug APK, optionally installs it on a connected device or
emulator, drives the end-to-end test hook, captures screenshots and a
UI-hierarchy dump, and writes a manifest under
`artifacts/PLANTPOTTING-0001/manifest.txt`. The manifest is diffed against
`docs/sprints/expected-artifacts/PLANTPOTTING-0001.txt` — the script fails
on diff.

If no adb device is available, the script runs in "build-only" mode and
emits a manifest covering only the build artifact checks. Run the full
flow on CI's Gradle Managed Device path for binding evidence.
#>

$ErrorActionPreference = "Stop"

$pkg = "com.darkfactory.plantpotting"
$sprintId = "PLANTPOTTING-0001"
$artifactsDir = "artifacts/$sprintId"
$manifestPath = "$artifactsDir/manifest.txt"
$expectedManifest = "docs/sprints/expected-artifacts/$sprintId.txt"
$apkPath = "app/build/outputs/apk/debug/app-debug.apk"

if (-not (Test-Path "./gradlew.bat")) {
    Write-Error "gradlew.bat not found — run this script from the repository root."
}

New-Item -ItemType Directory -Force -Path $artifactsDir | Out-Null

# 1. Build the debug APK + run networking guard.
Write-Host "[1/5] Assembling debug APK + verifyNoNetworking..." -ForegroundColor Cyan
& ./gradlew.bat --no-daemon assembleDebug verifyNoNetworking
if ($LASTEXITCODE -ne 0) { Write-Error "gradle assembleDebug failed" }

if (-not (Test-Path $apkPath)) { Write-Error "APK not found at $apkPath" }

# 2. APK content checks.
Write-Host "[2/5] Inspecting APK contents..." -ForegroundColor Cyan
$tempApkDir = Join-Path $env:TEMP "plantpotting-apk-$([guid]::NewGuid().ToString())"
New-Item -ItemType Directory -Force -Path $tempApkDir | Out-Null
Expand-Archive -Path $apkPath -DestinationPath $tempApkDir -Force

$archetypesAsset = Join-Path $tempApkDir "assets/kb/archetypes.json"
$speciesAsset = Join-Path $tempApkDir "assets/kb/species.json"

$hasArchetypesAsset = Test-Path $archetypesAsset
$hasSpeciesAsset = Test-Path $speciesAsset

# 3. Optional: check connected adb device for live drive.
$adbDevicePresent = $false
try {
    $adbOutput = & adb devices 2>$null
    $deviceLines = $adbOutput | Select-String "device$" | Where-Object { $_ -notmatch "List of devices" }
    if ($deviceLines.Count -gt 0) { $adbDevicePresent = $true }
} catch {
    # adb not installed — fall back to build-only manifest.
}

$deviceScreenshotCount = 0
if ($adbDevicePresent) {
    Write-Host "[3/5] adb device detected — installing + driving the flow..." -ForegroundColor Cyan
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
    Write-Host "[3/5] No adb device — build-only manifest." -ForegroundColor Yellow
}

# 4. Write the manifest.
Write-Host "[4/5] Writing manifest..." -ForegroundColor Cyan
$manifestLines = @(
    "apk-exists=$([bool](Test-Path $apkPath))".ToLower(),
    "archetypes-asset-present=$hasArchetypesAsset".ToLower(),
    "species-asset-present=$hasSpeciesAsset".ToLower(),
    "verify-no-networking-passed=true"
)
if ($adbDevicePresent) {
    $manifestLines += "device-screenshot-count-at-least-1=$($deviceScreenshotCount -ge 1)".ToLower()
}
Set-Content -Path $manifestPath -Value $manifestLines -Encoding utf8

# 5. Diff against expected.
Write-Host "[5/5] Diffing against $expectedManifest..." -ForegroundColor Cyan
if (-not (Test-Path $expectedManifest)) {
    Write-Error "expected manifest missing at $expectedManifest"
}
$expected = (Get-Content $expectedManifest) | Where-Object {
    $_ -and (-not $_.StartsWith("#"))
} | ForEach-Object { $_.Trim().ToLower() }
$actual = $manifestLines | ForEach-Object { $_.Trim().ToLower() }

$missingFromActual = $expected | Where-Object { $actual -notcontains $_ }
if ($missingFromActual) {
    Write-Host "Expected lines missing from actual manifest:" -ForegroundColor Red
    $missingFromActual | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    exit 1
}
Write-Host "Integration manifest diff passed." -ForegroundColor Green
