# Reference image attribution manifest

PLANTPOTTING-0010 Phase 6 / D4. Every bundled reference image under
`app/src/main/res/drawable-nodpi/*.webp` **must** have a row in the table below, and every image must
be **CC0 or public-domain** (PD). CI-enforced by
`ReferenceImageManifestTest.everyBundledReferenceImageHasAManifestEntry`.

## License policy (D4)

- **CC0 / public-domain only.** Sourced from **Wikimedia Commons** via
  `scripts/source-reference-images.ps1`, which filters the search results to `LicenseShortName ∈
  {CC0, Public domain, PD, No restrictions}` and skips everything else (CC-BY-SA etc.). The script is
  re-runnable.
- **No self-shot / first-party imagery** (sprint non-goal).
- **Format/budget:** downscaled **WebP** (≤480 px, ~quality 60), rendered with `painterResource` (no
  image library / Coil — network-free). Total ≈ **1.09 MiB** over the Phase-0 baseline — well inside
  the ≤3–5 MiB budget.

## Coverage

**All 44 KB species** have a license-clean image (a mix of CC0 photographs and public-domain
botanical plates — Curtis's, Blanco's, Aublet's, etc.). The authored placeholder vector
(`res/drawable/ic_plant_placeholder.xml`) remains the safety net for any future species. The first
pass filled 34; `source-missing-images.ps1` (broader Commons search) filled the remaining 10.

## How to add / refresh

Run `pwsh scripts/source-reference-images.ps1` (edit the `$targets` map to add species), then
register new files in `PlantImageResolver.images` and add their rows below.

## Manifest

| Image file (`drawable-nodpi/`) | KB speciesId | License | Source (Wikimedia Commons) |
|---|---|---|---|
| monstera_deliciosa.webp | monstera-deliciosa | CC0 | File:Monstera deliciosa - zrající plodenství.jpg |
| monstera_adansonii.webp | monstera-adansonii | Public domain | File:Monstera adansonii CBM.png |
| epipremnum_aureum.webp | epipremnum-aureum | CC0 | File:Epipremnum aureum in wild.jpg |
| spathiphyllum_wallisii.webp | spathiphyllum-wallisii | Public domain | File:SpathiphyllumWallisii.jpg |
| ficus_elastica.webp | ficus-elastica | CC0 | File:Ficus elastica 29761125.jpg |
| ficus_lyrata.webp | ficus-lyrata | CC0 | File:Ficus lyrata 348073320.jpg |
| dracaena_trifasciata.webp | dracaena-trifasciata | CC0 | File:Snake plant (Sansevieria trifasciata) Waoleona Buton Island 02.jpg |
| zamioculcas_zamiifolia.webp | zamioculcas-zamiifolia | CC0 | File:Millonaria (Zamioculcas zamiifolia).jpg |
| crassula_ovata.webp | crassula-ovata | CC0 | File:Among the branches of a potted jade plant.jpg |
| chlorophytum_comosum.webp | chlorophytum-comosum | CC0 | File:Seed and Fruit of Chlorophytum comosum.jpg |
| saintpaulia_ionantha.webp | saintpaulia-ionantha | Public domain | File:Streptocarpus ionanthus (as Saintpaulia ionantha) - Curtis' 121 pl. 7408 (1895).jpg |
| goeppertia_orbifolia.webp | goeppertia-orbifolia | CC0 | File:Calathea orbiculata - Wellington Botanic Garden - DSC09392.jpg |
| hoya_carnosa.webp | hoya-carnosa | CC0 | File:Hoya carnosa (wax vines).jpg |
| aglaonema.webp | aglaonema | CC0 | File:Konya Kelebekler Vadisi Aglaonema.jpg |
| anthurium_andraeanum.webp | anthurium-andraeanum | Public domain | File:Branched spadix of flamingo flower (Anthurium andraeanum).jpg |
| dieffenbachia.webp | dieffenbachia | CC0 | File:Dieffenbachia moralis holotype 01.jpg |
| aloe_vera.webp | aloe-vera | CC0 | File:Aloe vera for sale.jpg |
| kalanchoe.webp | kalanchoe | CC0 | File:Kalanchoe blossfeldiana (Florist Kalanchoe).jpg |
| maranta_leuconeura.webp | maranta-leuconeura | Public domain | File:Maranta leuconeura D2411.jpg |
| nephrolepis_exaltata.webp | nephrolepis-exaltata | CC0 | File:Helecho de Boston (Nephrolepis exaltata).jpg |
| pachira_aquatica.webp | pachira-aquatica | Public domain | File:Pachira aquatica Aublet 1775 pl 292.jpg |
| dypsis_lutescens.webp | dypsis-lutescens | CC0 | File:Dypsis lutescens Medellín - 4.jpg |
| tradescantia.webp | tradescantia | CC0 | File:Tradescantia zebrina (Maligano Buton Island).jpg |
| schefflera.webp | schefflera | CC0 | File:Schefflera arboricola (bonsai), Phipps Conservatory, 2014-03-01.jpg |
| euphorbia_pulcherrima.webp | euphorbia-pulcherrima | Public domain | File:Euphorbia pulcherrima Blanco1.167.jpg |
| dionaea_muscipula.webp | dionaea-muscipula | Public domain | File:Lavradia glandulosa Dionaea muscipula HistPlRemarqBresil 6(7).jpg |
| chamaedorea_elegans.webp | chamaedorea-elegans | Public domain | File:Chamaedorea elegans.png |
| strelitzia_reginae.webp | strelitzia-reginae | Public domain | File:Strelitzia larger.jpg |
| aspidistra_elatior.webp | aspidistra-elatior | Public domain | File:Aspidistra-elatior-variegata.jpg |
| asparagus_setaceus.webp | asparagus-setaceus | Public domain | File:Asparagus plumosa WPC.jpg |
| begonia.webp | begonia | Public domain | File:Begonia obliqua00.jpg |
| hypoestes_phyllostachya.webp | hypoestes-phyllostachya | Public domain | File:2006 08 14 Hypoestes Phyllostachya.jpg |
| beaucarnea_recurvata.webp | beaucarnea-recurvata | CC0 | File:Beaucarnea recurvata serrated leaf margin.jpg |
| schlumbergera_bridgesii.webp | schlumbergera-bridgesii | CC0 | File:Christmas Cactus October 2022.jpg |
| philodendron_hederaceum.webp | philodendron-hederaceum | CC0 | File:Philodendron sp.jpg |
| philodendron_pink_princess.webp | philodendron-pink-princess | CC0 | File:Pink princess philodendron.jpg (Cmushore) |
| phalaenopsis.webp | phalaenopsis | Public domain | File:Flower of Phalaenopsis mannii.JPG |
| alocasia.webp | alocasia | CC0 | File:Alocasia macrorrhizos 'Nigra' 02.jpg |
| dracaena.webp | dracaena | Public domain | File:Dracaena fragrans Curtis 1808 v27.jpg |
| hedera_helix.webp | hedera-helix | Public domain | File:Hedera helix lombozata.jpg |
| asplenium_nidus.webp | asplenium-nidus | Public domain | File:Neottopteris nidus - Kunming Botanical Garden - DSC03126.JPG |
| cycas_revoluta.webp | cycas-revoluta | Public domain | File:Center of sago palm (Cycas revoluta) close-up.jpg |
| yucca.webp | yucca | Public domain | File:Yucca elephantipes a P. Letamendi.JPG |
| ctenanthe.webp | ctenanthe | CC0 | File:Ctenanthe oppenheimiana - Shinjuku Gyo-en Greenhouse - DSC05761.jpg |

_Full source URLs (descriptionurl per file) are recorded in
`docs/sprints/feedback/PLANTPOTTING-0010/sourced-images.tsv`._

## PLANTPOTTING-0011 — botanical-plate → photograph swaps

The 0010 handoff flagged five reference images that were public-domain **botanical plates** rather
than photographs (peace lily, poinsettia, parlor palm, dracaena, philodendron pink-princess). A
CC0/PD **photograph** was sourced where one exists; otherwise the plate is retained and the reason
recorded (CC0/PD-only discipline — `scripts/source-reference-images.ps1` filtering; no CC-BY).

| target | outcome |
|---|---|
| philodendron-pink-princess | **SWAPPED** → CC0 photo `File:Pink princess philodendron.jpg` (Cmushore). The old plate depicted *P. erubescens* (the green species), not the pink cultivar — the photo is strictly better. |
| spathiphyllum-wallisii (peace lily) | **Plate retained** — no CC0/PD photograph among the top 40 Commons results (all CC-BY/CC-BY-SA/GFDL). |
| euphorbia-pulcherrima (poinsettia) | **Plate retained** — only CC0/PD results are illustrations/line art (PSF, Blanco plate); no photograph. |
| chamaedorea-elegans (parlor palm) | **Plate retained** — only CC0/PD results are botanical plates (*Flore des serres*, *Historia naturalis palmarum*); no photograph. |
| dracaena (*D. marginata*) | **Plate retained** — no CC0/PD photograph among the top 40 Commons results. |
