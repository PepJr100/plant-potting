#requires -version 5.1
# Adb shim for PLANTPOTTING-0003 §7.2 test: on the FIRST `shell uiautomator dump`
# invocation, prints the documented null-root stderr and exits 0 without writing
# the dump file. On every subsequent invocation, writes a stub UI XML to a
# state path so the matching `pull` can copy it locally and Wait-ForNode
# resolves the node.
#
# Returned as a script block (`.GetNewClosure()` captures the state-dir locals);
# `& $shim args...` mimics `& $adb args...` from the caller's perspective.

Set-StrictMode -Version Latest

function Get-NullRootThenOkShim {
    [CmdletBinding()]
    param(
        [string]$StateDir = (Join-Path $env:TEMP "adb-shim-null-root-then-ok"),
        [string]$NodeXml = '<?xml version="1.0" encoding="UTF-8"?><hierarchy><node resource-id="test.target" bounds="[0,0][10,10]"/></hierarchy>'
    )
    if (Test-Path $StateDir) { Remove-Item -Recurse -Force $StateDir }
    New-Item -ItemType Directory -Force -Path $StateDir | Out-Null
    $counterFile = Join-Path $StateDir "counter.txt"
    $deviceXmlFile = Join-Path $StateDir "device-ui.xml"
    Set-Content -Path $counterFile -Value "0" -Encoding ascii -NoNewline

    return {
        param([Parameter(ValueFromRemainingArguments)] [string[]]$Args)
        if ($Args.Length -ge 3 -and $Args[0] -eq "shell" -and $Args[1] -eq "uiautomator" -and $Args[2] -eq "dump") {
            $counter = [int](Get-Content $counterFile)
            $counter++
            Set-Content -Path $counterFile -Value "$counter" -Encoding ascii -NoNewline
            if ($counter -eq 1) {
                [Console]::Error.WriteLine("ERROR: null root node returned by UiTestAutomationBridge.")
                $global:LASTEXITCODE = 0
                return
            }
            Set-Content -Path $deviceXmlFile -Value $NodeXml -Encoding utf8
            $global:LASTEXITCODE = 0
            return
        }
        if ($Args.Length -ge 3 -and $Args[0] -eq "pull" -and $Args[1] -eq "/sdcard/ui.xml") {
            $localPath = $Args[2]
            if (Test-Path $deviceXmlFile) {
                Copy-Item -Path $deviceXmlFile -Destination $localPath -Force
                $global:LASTEXITCODE = 0
                return
            }
            $global:LASTEXITCODE = 1
            return
        }
        $global:LASTEXITCODE = 0
    }.GetNewClosure()
}
