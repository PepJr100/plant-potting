#requires -version 5.1
# Helpers for `scripts/integration-flow.ps1`. Extracted into a dot-source-able
# module so the PLANTPOTTING-0003 §7.2 test harness can drive Wait-ForNode
# against mock shims without spawning the full integration flow.
#
# PLANTPOTTING-0003 §4.8 / §7.3 fix: `Invoke-AdbDump` now returns a boolean
# instead of throwing on every miss, so `Wait-ForNode`'s retry loop can
# actually retry. Hard failures (adb itself missing, non-retryable error)
# still throw.

Set-StrictMode -Version Latest

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

# PLANTPOTTING-0002 Bug A: `uiautomator dump` returns exit 0 even when it
# writes `ERROR: null root node returned by UiTestAutomationBridge` to
# stderr (the canonical Android-side symptom of dumping mid-foreground-
# transition). The previous Invoke-AdbDump only checked $LASTEXITCODE,
# so the soft failure (no dump file written) surfaced one step later when
# `adb pull` failed against a non-existent device file — and Invoke-AdbDump
# unconditionally threw, so Wait-ForNode's retry loop never iterated.
#
# Fix shape (PLANTPOTTING-0003 §4.8):
#   - Capture `uiautomator dump`'s stderr via `2>&1` and inspect for the
#     documented null-root string. Match → soft failure, return $false.
#   - Treat `dump exit 0` with `adb pull` failing → soft failure, return $false.
#   - Hard failures (adb itself missing on PATH, non-retryable transport
#     errors) still throw.
function Invoke-AdbDump {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] $adb,
        [Parameter(Mandatory)] [string]$path
    )
    # Drop any previous local file so a "pull succeeded but no new file"
    # case doesn't falsely look like progress.
    if (Test-Path $path) { Remove-Item -Path $path -Force }

    $dumpOutput = & $adb shell uiautomator dump /sdcard/ui.xml 2>&1
    $dumpExit = $LASTEXITCODE
    $dumpJoined = ($dumpOutput | ForEach-Object { "$_" }) -join "`n"

    # Documented Android race string. Match → soft failure regardless of
    # exit code (the dump exited cleanly but produced no file).
    if ($dumpJoined -match "null root node returned by UiTestAutomationBridge") {
        Write-Verbose "Invoke-AdbDump: null-root race observed; soft failure"
        return $false
    }
    if ($dumpExit -ne 0) {
        # Real dump error that wasn't the documented race. Surface it.
        throw "uiautomator dump failed (exit $dumpExit): $dumpJoined"
    }

    & $adb pull /sdcard/ui.xml $path 2>&1 | Out-Null
    $pullExit = $LASTEXITCODE
    if ($pullExit -ne 0) {
        Write-Verbose "Invoke-AdbDump: adb pull exit $pullExit; soft failure"
        return $false
    }
    if (-not (Test-Path $path)) {
        # dump exit 0, stderr clean, pull exit 0, but the file isn't here.
        # That's a soft failure too — retry.
        Write-Verbose "Invoke-AdbDump: dump file missing after pull; soft failure"
        return $false
    }
    return $true
}

# PLANTPOTTING-0003 §4.8 retry policy: poll up to maxAttempts dumps, with
# the documented race or soft failure looping rather than throwing. Hard
# failures inside Invoke-AdbDump still propagate. Default maxAttempts
# bumped from 8 to 12 because the AOSP image's cold-launch foreground
# transition can take longer than 8 * 750 ms.
function Wait-ForNode {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] $adb,
        [Parameter(Mandatory)] [string]$resourceId,
        [Parameter(Mandatory)] [string]$dumpPath,
        [int]$maxAttempts = 12,
        [int]$retryDelayMs = 750
    )
    for ($i = 1; $i -le $maxAttempts; $i++) {
        $ok = Invoke-AdbDump -adb $adb -path $dumpPath
        if ($ok) {
            $doc = New-Object System.Xml.XmlDocument
            $doc.Load((Resolve-Path $dumpPath))
            $node = $doc.SelectSingleNode("//node[@resource-id='$resourceId']")
            if ($null -ne $node) { return $node }
        }
        if ($i -lt $maxAttempts) {
            Start-Sleep -Milliseconds $retryDelayMs
        }
    }
    throw "Timed out waiting for node with resource-id='$resourceId' after $maxAttempts dumps"
}
