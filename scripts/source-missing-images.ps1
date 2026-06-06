# PLANTPOTTING-0010 — second pass for species the first run couldn't fill: broader Commons search
# (limit 50 + alternate query terms), CC0/PD only. Appends to sourced-images.tsv.

$ErrorActionPreference = 'Stop'
$root = "D:\DarkFactoryProject\Plant potting"
$outDir = Join-Path $root "app\src\main\res\drawable-nodpi"
$tmpDir = Join-Path $env:TEMP "pp-refimg"
$tsv = Join-Path $root "docs\sprints\feedback\PLANTPOTTING-0010\sourced-images.tsv"
New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null
$ff = (Get-ChildItem "$env:LOCALAPPDATA\Microsoft\WinGet\Packages" -Recurse -Filter ffmpeg.exe -ErrorAction SilentlyContinue | Select-Object -First 1).FullName
$ua = 'PlantPotting-dev/0.4 (houseplant app; reference-image sourcing; contact whichrobevans@gmail.com)'

# speciesId -> ordered list of query terms to try
$targets = [ordered]@{
  'philodendron-hederaceum'   = @('Philodendron hederaceum', 'Philodendron scandens', 'Philodendron hederaceum leaves')
  'philodendron-pink-princess'= @('Philodendron erubescens', 'Philodendron Pink Princess')
  'phalaenopsis'              = @('Phalaenopsis flower', 'Phalaenopsis amabilis', 'Phalaenopsis')
  'alocasia'                  = @('Alocasia macrorrhizos', 'Alocasia plant', 'Alocasia')
  'dracaena'                  = @('Dracaena fragrans', 'Dracaena marginata', 'Dracaena')
  'hedera-helix'              = @('Hedera helix leaves', 'Hedera helix plant', 'Hedera helix')
  'asplenium-nidus'           = @('Asplenium nidus plant', 'Asplenium nidus')
  'cycas-revoluta'            = @('Cycas revoluta plant', 'Cycas revoluta')
  'yucca'                     = @('Yucca gigantea', 'Yucca elephantipes', 'Yucca aloifolia')
  'ctenanthe'                 = @('Ctenanthe oppenheimiana', 'Ctenanthe burle-marxii', 'Ctenanthe')
}

function Get-Clean($q) {
  $u = "https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search&gsrsearch=filetype:bitmap%20$([uri]::EscapeDataString($q))&gsrnamespace=6&gsrlimit=50&prop=imageinfo&iiprop=url|extmetadata&iiurlwidth=520"
  $r = Invoke-RestMethod -Uri $u -Headers @{ 'User-Agent' = $ua } -TimeoutSec 40
  if (-not $r.query) { return $null }
  $pages = @($r.query.pages.PSObject.Properties.Value) | Sort-Object index
  foreach ($p in $pages) {
    $ii = $p.imageinfo[0]
    $lic = "$($ii.extmetadata.LicenseShortName.value)".Trim()
    if ($lic -match '^(CC0|Public domain|PD|No restrictions)') {
      return @{ url = $ii.thumburl; lic = $lic; title = $p.title; desc = $ii.descriptionurl }
    }
  }
  return $null
}

$ok = 0; $skip = 0
foreach ($id in $targets.Keys) {
  $drawable = ($id -replace '-', '_')
  $chosen = $null
  foreach ($q in $targets[$id]) {
    try { $chosen = Get-Clean $q } catch { $chosen = $null }
    if ($chosen) { break }
  }
  if (-not $chosen) { "SKIP  $id"; $skip++; continue }
  try {
    $tmp = Join-Path $tmpDir "$drawable.src"
    Invoke-WebRequest -Uri $chosen.url -Headers @{ 'User-Agent' = $ua } -OutFile $tmp -TimeoutSec 60
    $webp = Join-Path $outDir "$drawable.webp"
    & $ff -y -hide_banner -loglevel error -i $tmp -vf "scale=440:-1" -c:v libwebp -quality 60 $webp
    if (-not (Test-Path $webp)) { "FAIL  $id"; $skip++; continue }
    $kb = [math]::Round((Get-Item $webp).Length / 1KB, 1)
    "$id`t$drawable.webp`t$($chosen.lic)`t$($chosen.title)`t$($chosen.desc)" | Add-Content -Path $tsv -Encoding UTF8
    "OK    $id  ->  $drawable.webp  ${kb}KB  [$($chosen.lic)]"
    $ok++
  } catch { "ERR   $id  $($_.Exception.Message)"; $skip++ }
}
""
"DONE: $ok sourced, $skip skipped."