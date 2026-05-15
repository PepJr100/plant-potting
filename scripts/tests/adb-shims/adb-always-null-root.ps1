#requires -version 5.1
# Adb shim for PLANTPOTTING-0003 §7.2 test: every `shell uiautomator dump`
# invocation prints the documented null-root stderr and exits 0 without
# writing the dump file. Used to prove `Wait-ForNode` throws after
# `maxAttempts` exhaustion (no infinite loops).

Set-StrictMode -Version Latest

function Get-AlwaysNullRootShim {
    [CmdletBinding()] param()
    return {
        param([Parameter(ValueFromRemainingArguments)] [string[]]$Args)
        if ($Args.Length -ge 3 -and $Args[0] -eq "shell" -and $Args[1] -eq "uiautomator" -and $Args[2] -eq "dump") {
            [Console]::Error.WriteLine("ERROR: null root node returned by UiTestAutomationBridge.")
            $global:LASTEXITCODE = 0
            return
        }
        if ($Args.Length -ge 3 -and $Args[0] -eq "pull") {
            # File never exists.
            $global:LASTEXITCODE = 1
            return
        }
        $global:LASTEXITCODE = 0
    }
}
