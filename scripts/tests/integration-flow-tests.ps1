#requires -version 5.1
# PLANTPOTTING-0003 §7.2 test harness for `scripts/integration-flow-helpers.ps1`.
#
# Plain `.ps1` form (Pester 5 isn't on this machine's default module path; the
# fallback per §7.2 is a script that exit-codes on failure).
#
# Usage:
#   pwsh ./scripts/tests/integration-flow-tests.ps1
# Exit code 0 = all tests pass; non-zero = at least one failed.

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

. "$PSScriptRoot/../integration-flow-helpers.ps1"
. "$PSScriptRoot/adb-shims/adb-null-root-then-ok.ps1"
. "$PSScriptRoot/adb-shims/adb-always-null-root.ps1"

$script:passes = 0
$script:fails = 0

function Test-Case {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [scriptblock]$Body
    )
    try {
        & $Body
        $script:passes++
        Write-Host "PASS  $Name" -ForegroundColor Green
    } catch {
        $script:fails++
        Write-Host "FAIL  $Name" -ForegroundColor Red
        Write-Host "      $($_.Exception.Message)" -ForegroundColor Red
    }
}

function Assert-Equal {
    param($Expected, $Actual, [string]$Message = "")
    if ($Expected -ne $Actual) {
        throw "Expected '$Expected' but was '$Actual'. $Message"
    }
}

function Assert-True {
    param($Value, [string]$Message = "")
    if (-not $Value) { throw "Expected truthy value. $Message" }
}

function Assert-Throws {
    param([Parameter(Mandatory)] [scriptblock]$Body, [string]$Match = "")
    try {
        & $Body
        throw "Expected the block to throw, but it did not."
    } catch {
        if ($Match -ne "" -and $_.Exception.Message -notmatch $Match) {
            throw "Expected throw message to match '$Match' but was: $($_.Exception.Message)"
        }
    }
}

$tempDumpPath = Join-Path $env:TEMP "integration-flow-tests-dump.xml"

Test-Case "Wait-ForNode succeeds on the second dump after a null-root race" {
    $adb = Get-NullRootThenOkShim
    if (Test-Path $tempDumpPath) { Remove-Item -Force $tempDumpPath }
    $node = Wait-ForNode -adb $adb -resourceId "test.target" -dumpPath $tempDumpPath -maxAttempts 5 -retryDelayMs 10
    Assert-True ($null -ne $node) "Wait-ForNode returned null"
    Assert-Equal "test.target" $node.GetAttribute("resource-id")
}

Test-Case "Wait-ForNode throws after maxAttempts against a perpetually-null-root shim" {
    $adb = Get-AlwaysNullRootShim
    if (Test-Path $tempDumpPath) { Remove-Item -Force $tempDumpPath }
    Assert-Throws -Body {
        Wait-ForNode -adb $adb -resourceId "test.target" -dumpPath $tempDumpPath -maxAttempts 3 -retryDelayMs 10
    } -Match "Timed out"
}

Test-Case "Invoke-AdbDump returns false on the null-root race without throwing" {
    $adb = Get-AlwaysNullRootShim
    if (Test-Path $tempDumpPath) { Remove-Item -Force $tempDumpPath }
    $ok = Invoke-AdbDump -adb $adb -path $tempDumpPath
    Assert-Equal $false $ok
}

Test-Case "Invoke-AdbDump returns true on a clean dump" {
    $adb = Get-NullRootThenOkShim
    if (Test-Path $tempDumpPath) { Remove-Item -Force $tempDumpPath }
    # First invocation is the race → false.
    $first = Invoke-AdbDump -adb $adb -path $tempDumpPath
    Assert-Equal $false $first
    # Second invocation should succeed.
    $second = Invoke-AdbDump -adb $adb -path $tempDumpPath
    Assert-Equal $true $second
    Assert-True (Test-Path $tempDumpPath) "Dump file did not land at $tempDumpPath"
}

Test-Case "Invoke-AdbDump clears any stale local file before each dump attempt" {
    $adb = Get-AlwaysNullRootShim
    # Pre-seed a stale file at the target path.
    Set-Content -Path $tempDumpPath -Value "<stale/>" -Encoding utf8
    $ok = Invoke-AdbDump -adb $adb -path $tempDumpPath
    Assert-Equal $false $ok
    # The stale file must be gone so a stale Test-Path doesn't fake success.
    Assert-True (-not (Test-Path $tempDumpPath)) "Stale dump file was not removed"
}

Write-Host ""
Write-Host ("{0} passed, {1} failed" -f $script:passes, $script:fails) `
    -ForegroundColor $(if ($script:fails -eq 0) { "Green" } else { "Red" })
if ($script:fails -gt 0) { exit 1 } else { exit 0 }
