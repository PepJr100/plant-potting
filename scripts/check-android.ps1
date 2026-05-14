#requires -version 5.1
# Local mirror of CI: assembleDebug + unit tests + lint + ktlint + GMD + networking guard.
# Fails fast on the first non-zero exit. Run from the repository root.

$ErrorActionPreference = "Stop"

if (-not (Test-Path "./gradlew.bat")) {
    Write-Error "gradlew.bat not found. Run this script from the repository root."
}

$tasks = @(
    "assembleDebug",
    "testDebugUnitTest",
    "lint",
    "ktlintCheck",
    "pixel6Api34DebugAndroidTest",
    "verifyNoNetworking"
)

Write-Host "Running: ./gradlew $($tasks -join ' ')" -ForegroundColor Cyan
& ./gradlew.bat --no-daemon @tasks
if ($LASTEXITCODE -ne 0) {
    Write-Error "Gradle failed with exit code $LASTEXITCODE"
}

Write-Host "All checks passed." -ForegroundColor Green
