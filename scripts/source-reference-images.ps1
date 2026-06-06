# PLANTPOTTING-0010 — source CC0/Public-domain reference images from Wikimedia Commons,
# convert to ~480px WebP under app/src/main/res/drawable-nodpi/, and emit a manifest TSV.
# Dev-time asset acquisition only — NOT app-runtime networking (verifyNoNetworking unaffected).
# Re-runnable: only CC0 / Public domain results are accepted; everything else is skipped.

$ErrorActionPreference = 'Stop'
$root = "D:\DarkFactoryProject\Plant potting"
$outDir = Join-Path $root "app\src\main\res\drawable-nodpi"
$tmpDir = Join-Path $env:TEMP "pp-refimg"
$tsv = Join-Path $root "docs\sprints\feedback\PLANTPOTTING-0010\sourced-images.tsv"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null
$ff = (Get-ChildItem "$env:LOCALAPPDATA\Microsoft\WinGet\Packages" -Recurse -Filter ffmpeg.exe -ErrorAction SilentlyContinue | Select-Object -First 1).FullName

# speciesId  ->  Commons search query
$targets = [ordered]@{
  'monstera-deliciosa'        = 'Monstera deliciosa'
  'monstera-adansonii'        = 'Monstera adansonii'
  'epipremnum-aureum'         = 'Epipremnum aureum'
  'philodendron-hederaceum'   = 'Philodendron hederaceum'
  'spathiphyllum-wallisii'    = 'Spathiphyllum wallisii'
  'ficus-elastica'            = 'Ficus elastica'
  'ficus-lyrata'              = 'Ficus lyrata'
  'dracaena-trifasciata'      = 'Sansevieria trifasciata'
  'zamioculcas-zamiifolia'    = 'Zamioculcas zamiifolia'
  'crassula-ovata'            = 'Crassula ovata'
  'chlorophytum-comosum'      = 'Chlorophytum comosum'
  'saintpaulia-ionantha'      = 'Saintpaulia ionantha'
  'phalaenopsis'              = 'Phalaenopsis'
  'goeppertia-orbifolia'      = 'Goeppertia orbifolia'
  'hoya-carnosa'              = 'Hoya carnosa'
  'aglaonema'                 = 'Aglaonema'
  'alocasia'                  = 'Alocasia'
  'anthurium-andraeanum'      = 'Anthurium andraeanum'
  'dieffenbachia'             = 'Dieffenbachia'
  'aloe-vera'                 = 'Aloe vera'
  'kalanchoe'                 = 'Kalanchoe blossfeldiana'
  'maranta-leuconeura'        = 'Maranta leuconeura'
  'nephrolepis-exaltata'      = 'Nephrolepis exaltata'
  'pachira-aquatica'          = 'Pachira aquatica'
  'dypsis-lutescens'          = 'Dypsis lutescens'
  'dracaena'                  = 'Dracaena marginata'
  'tradescantia'              = 'Tradescantia zebrina'
  'hedera-helix'              = 'Hedera helix'
  'schefflera'                = 'Schefflera arboricola'
  'euphorbia-pulcherrima'     = 'Euphorbia pulcherrima'
  'dionaea-muscipula'         = 'Dionaea muscipula'
  'chamaedorea-elegans'       = 'Chamaedorea elegans'
  'strelitzia-reginae'        = 'Strelitzia reginae'
  'aspidistra-elatior'        = 'Aspidistra elatior'
  'asplenium-nidus'           = 'Asplenium nidus'
  'asparagus-setaceus'        = 'Asparagus setaceus'
  'begonia'                   = 'Begonia'
  'hypoestes-phyllostachya'   = 'Hypoestes phyllostachya'
  'beaucarnea-recurvata'      = 'Beaucarnea recurvata'
  'cycas-revoluta'            = 'Cycas revoluta'
  'yucca'                     = 'Yucca gigantea'
  'ctenanthe'                 = 'Ctenanthe'
  'schlumbergera-bridgesii'   = 'Schlumbergera'
}

$ua = 'PlantPotting-dev/0.4 (houseplant app; reference-image sourcing; contact whichrobevans@gmail.com)'
"speciesId`tfile`tlicense`ttitle`tdescriptionUrl" | Set-Content -Path $tsv -Encoding UTF8
$ok = 0; $skip = 0

foreach ($id in $targets.Keys) {
  $q = $targets[$id]
  $drawable = ($id -replace '-', '_')
  try {
    $u = "https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search&gsrsearch=filetype:bitmap%20$([uri]::EscapeDataString($q))&gsrnamespace=6&gsrlimit=15&prop=imageinfo&iiprop=url|extmetadata&iiurlwidth=520"
    $r = Invoke-RestMethod -Uri $u -Headers @{ 'User-Agent' = $ua } -TimeoutSec 40
    $pages = @($r.query.pages.PSObject.Properties.Value) | Sort-Object index
    $chosen = $null
    foreach ($p in $pages) {
      $ii = $p.imageinfo[0]
      $lic = "$($ii.extmetadata.LicenseShortName.value)".Trim()
      if ($lic -match '^(CC0|Public domain|PD|No restrictions)') {
        $chosen = @{ url = $ii.thumburl; lic = $lic; title = $p.title; desc = $ii.descriptionurl }
        break
      }
    }
    if (-not $chosen) { "SKIP  $id  (no CC0/PD among top results)"; $skip++; continue }

    $tmp = Join-Path $tmpDir "$drawable.src"
    Invoke-WebRequest -Uri $chosen.url -Headers @{ 'User-Agent' = $ua } -OutFile $tmp -TimeoutSec 60
    $webp = Join-Path $outDir "$drawable.webp"
    & $ff -y -hide_banner -loglevel error -i $tmp -vf "scale=480:-1" -c:v libwebp -quality 68 $webp
    if (-not (Test-Path $webp)) { "FAIL  $id  (ffmpeg produced no output)"; $skip++; continue }
    $kb = [math]::Round((Get-Item $webp).Length / 1KB, 1)
    "$id`t$drawable.webp`t$($chosen.lic)`t$($chosen.title)`t$($chosen.desc)" | Add-Content -Path $tsv -Encoding UTF8
    "OK    $id  ->  $drawable.webp  ${kb}KB  [$($chosen.lic)]"
    $ok++
  } catch {
    "ERR   $id  $($_.Exception.Message)"; $skip++
  }
}
""
"DONE: $ok sourced, $skip skipped. TSV: $tsv"