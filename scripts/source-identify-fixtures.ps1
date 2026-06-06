# PLANTPOTTING-0011 Phase 1a — source CC0/Public-domain real-photo candidates for the eval
# fixture set (app/src/androidTest/assets/identify-fixtures/). Dev-time asset acquisition only —
# NOT app-runtime networking (verifyNoNetworking unaffected).
#
# This DOWNLOADS candidates into a project-local temp dir for MANUAL VETTING (a wrong/cluttered
# photo would poison the scorecard). Only CC0 / Public domain results are kept; everything else is
# logged and skipped. After vetting, finalize.py crops/resizes the chosen ones to 480x480 JPEG q80.
#
# Re-runnable. Emits a TSV log of every candidate (license/title/url/localpath).

$ErrorActionPreference = 'Stop'
$root = "D:\DarkFactoryProject\Plant potting"
$tmpDir = Join-Path $root "app\build\pp-fixture-src"
$tsv = Join-Path $root "app\build\pp-fixture-src\candidates.tsv"
New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null

# kb-species-id -> Commons search query. In-vocab, mapped, popular species the principal would shoot.
# (dracaena-trifasciata / epipremnum-aureum already have one fixture each — these add a 2nd base photo.)
$targets = [ordered]@{
  'dracaena-trifasciata'  = 'Sansevieria trifasciata potted'
  'epipremnum-aureum'     = 'Epipremnum aureum pothos'
  'ficus-elastica'        = 'Ficus elastica rubber plant'
  'aloe-vera'             = 'Aloe vera potted plant'
  'hedera-helix'          = 'Hedera helix ivy houseplant'
  'euphorbia-pulcherrima' = 'Euphorbia pulcherrima poinsettia'
  'aglaonema'             = 'Aglaonema plant'
  'anthurium-andraeanum'  = 'Anthurium andraeanum'
  'dieffenbachia'         = 'Dieffenbachia plant'
  'dracaena'              = 'Dracaena marginata'
  'schefflera'            = 'Schefflera arboricola'
  'kalanchoe'             = 'Kalanchoe blossfeldiana'
}

$ua = 'PlantPotting-dev/0.5 (houseplant app; eval fixture sourcing; contact whichrobevans@gmail.com)'
"speciesId`tcandIndex`tlicense`ttitle`tdescriptionUrl`tlocalPath" | Set-Content -Path $tsv -Encoding UTF8
$ok = 0; $skip = 0

foreach ($id in $targets.Keys) {
  $q = $targets[$id]
  try {
    $u = "https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search&gsrsearch=filetype:bitmap%20$([uri]::EscapeDataString($q))&gsrnamespace=6&gsrlimit=40&prop=imageinfo&iiprop=url|extmetadata&iiurlwidth=900"
    $r = Invoke-RestMethod -Uri $u -Headers @{ 'User-Agent' = $ua } -TimeoutSec 50
    $pages = @($r.query.pages.PSObject.Properties.Value) | Sort-Object index
    $n = 0
    foreach ($p in $pages) {
      if ($n -ge 3) { break }
      $ii = $p.imageinfo[0]
      $lic = "$($ii.extmetadata.LicenseShortName.value)".Trim()
      if ($lic -notmatch '^(CC0|Public domain|PD|No restrictions)') { continue }
      $n++
      $dst = Join-Path $tmpDir "$($id)__cand$('{0:D2}' -f $n).jpg"
      try {
        Invoke-WebRequest -Uri $ii.thumburl -Headers @{ 'User-Agent' = $ua } -OutFile $dst -TimeoutSec 60
        "$id`t$n`t$lic`t$($p.title)`t$($ii.descriptionurl)`t$dst" | Add-Content -Path $tsv -Encoding UTF8
        "OK    $id  cand$n  [$lic]  $($p.title)"
        $ok++
      } catch { "ERRDL $id  $($_.Exception.Message)"; $skip++ }
    }
    if ($n -eq 0) { "SKIP  $id  (no CC0/PD among top 40 results)"; $skip++ }
  } catch {
    "ERR   $id  $($_.Exception.Message)"; $skip++
  }
}
""
"DONE: $ok candidates downloaded, $skip skips/errors. TSV: $tsv"