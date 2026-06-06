# Reference image attribution manifest

PLANTPOTTING-0010 Phase 6 / D4. Every bundled reference image under
`app/src/main/res/drawable-nodpi/*.webp` **must** have a row in the table below, and every image must
be **CC0 or public-domain** (PD). CI-enforced by
`ReferenceImageManifestTest.everyBundledReferenceImageHasAManifestEntry`.

## License policy (D4; relaxed in PLANTPOTTING-0011)

- **CC0 / public-domain preferred; CC BY and the Unsplash License also allowed.** Originally CC0/PD-only;
  the principal authorised **CC BY** (attribution required) and the **Unsplash License** (permissive;
  commercial + modify OK; no attribution required; only restriction is "don't build a competing stock-photo
  service", which we don't) for reference images in PLANTPOTTING-0011, so botanical-plate diagrams could be
  replaced with real photographs. **CC BY-SA is still NOT accepted** (share-alike). Sourced from **Wikimedia
  Commons** (CC0/PD first, CC BY fallback) and **Unsplash** (for the two species with no clean Commons photo).
- **Attribution:** every CC BY image MUST have a row in the bundled `app/src/main/assets/image_credits.tsv`
  and is shown in-app on the **Image credits** screen (legal requirement of CC BY). CC0/PD images need no
  attribution. Enforced by `ImageCreditsTest` + `ReferenceImageManifestTest`.
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
| monstera_adansonii.webp | monstera-adansonii | CC0 | File:Monstera adansonii 112059105.jpg (Jacob Rehage) |
| epipremnum_aureum.webp | epipremnum-aureum | CC0 | File:Epipremnum aureum in wild.jpg |
| spathiphyllum_wallisii.webp | spathiphyllum-wallisii | Public domain | File:SpathiphyllumWallisii.jpg |
| ficus_elastica.webp | ficus-elastica | CC0 | File:Ficus elastica 29761125.jpg |
| ficus_lyrata.webp | ficus-lyrata | CC0 | File:Ficus lyrata 348073320.jpg |
| dracaena_trifasciata.webp | dracaena-trifasciata | CC0 | File:Snake plant (Sansevieria trifasciata) Waoleona Buton Island 02.jpg |
| zamioculcas_zamiifolia.webp | zamioculcas-zamiifolia | CC0 | File:Millonaria (Zamioculcas zamiifolia).jpg |
| crassula_ovata.webp | crassula-ovata | CC0 | File:Among the branches of a potted jade plant.jpg |
| chlorophytum_comosum.webp | chlorophytum-comosum | CC0 | File:Spider plant with plantlets and flowers.jpg (W.carter) |
| saintpaulia_ionantha.webp | saintpaulia-ionantha | CC0 | File:Saintpaulia ionantha subsp. velutina - Copenhagen Botanical Garden - DSC07434.JPG (Daderot) |
| goeppertia_orbifolia.webp | goeppertia-orbifolia | CC0 | File:Calathea orbiculata - Wellington Botanic Garden - DSC09392.jpg |
| hoya_carnosa.webp | hoya-carnosa | CC0 | File:Hoya carnosa (wax vines).jpg |
| aglaonema.webp | aglaonema | CC0 | File:Konya Kelebekler Vadisi Aglaonema.jpg |
| anthurium_andraeanum.webp | anthurium-andraeanum | CC BY 3.0 | File:Starr-100623-7786-Anthurium andraeanum-red flowers potted plants in shade house (Forest & Kim Starr) |
| dieffenbachia.webp | dieffenbachia | CC0 | File:Dieffenbachia moralis holotype 01.jpg |
| aloe_vera.webp | aloe-vera | CC0 | File:Aloe vera for sale.jpg |
| kalanchoe.webp | kalanchoe | CC0 | File:Kalanchoe blossfeldiana (Florist Kalanchoe).jpg |
| maranta_leuconeura.webp | maranta-leuconeura | Public domain | File:Maranta leuconeura D2411.jpg |
| nephrolepis_exaltata.webp | nephrolepis-exaltata | CC0 | File:Helecho de Boston (Nephrolepis exaltata).jpg |
| pachira_aquatica.webp | pachira-aquatica | CC BY 4.0 | File:Money tree (Pachira aquatica).gif (Atlas Þə Biologist) |
| dypsis_lutescens.webp | dypsis-lutescens | CC0 | File:Dypsis lutescens Medellín - 4.jpg |
| tradescantia.webp | tradescantia | CC0 | File:Tradescantia zebrina (Maligano Buton Island).jpg |
| schefflera.webp | schefflera | CC0 | File:Schefflera arboricola (bonsai), Phipps Conservatory, 2014-03-01.jpg |
| euphorbia_pulcherrima.webp | euphorbia-pulcherrima | Unsplash License | Unsplash — Jessica Fadel (https://unsplash.com/photos/2B3quIShwJM) |
| dionaea_muscipula.webp | dionaea-muscipula | CC0 | File:Venus flytrap in pot.jpg (Deltapug) |
| chamaedorea_elegans.webp | chamaedorea-elegans | Public domain | File:Chamaedoreaaelegans.jpg (Biotaman) |
| strelitzia_reginae.webp | strelitzia-reginae | Public domain | File:Strelitzia larger.jpg |
| aspidistra_elatior.webp | aspidistra-elatior | Public domain | File:Aspidistra-elatior-variegata.jpg |
| asparagus_setaceus.webp | asparagus-setaceus | Public domain | File:Asparagus plumosa WPC.jpg |
| begonia.webp | begonia | Unsplash License | Unsplash — Sanni Sahil (https://unsplash.com/photos/KcUNr5_DK5A) |
| hypoestes_phyllostachya.webp | hypoestes-phyllostachya | Public domain | File:2006 08 14 Hypoestes Phyllostachya.jpg |
| beaucarnea_recurvata.webp | beaucarnea-recurvata | CC0 | File:Beaucarnea recurvata serrated leaf margin.jpg |
| schlumbergera_bridgesii.webp | schlumbergera-bridgesii | CC0 | File:Christmas Cactus October 2022.jpg |
| philodendron_hederaceum.webp | philodendron-hederaceum | CC0 | File:Philodendron sp.jpg |
| philodendron_pink_princess.webp | philodendron-pink-princess | CC0 | File:Pink princess philodendron.jpg (Cmushore) |
| phalaenopsis.webp | phalaenopsis | Public domain | File:Flower of Phalaenopsis mannii.JPG |
| alocasia.webp | alocasia | CC0 | File:Alocasia macrorrhizos 'Nigra' 02.jpg |
| dracaena.webp | dracaena | CC BY 3.0 | File:Starr 061231-3032 Dracaena marginata.jpg (Forest & Kim Starr) |
| hedera_helix.webp | hedera-helix | Public domain | File:Hedera helix lombozata.jpg |
| asplenium_nidus.webp | asplenium-nidus | Public domain | File:Neottopteris nidus - Kunming Botanical Garden - DSC03126.JPG |
| cycas_revoluta.webp | cycas-revoluta | Public domain | File:Center of sago palm (Cycas revoluta) close-up.jpg |
| yucca.webp | yucca | Public domain | File:Yucca elephantipes a P. Letamendi.JPG |
| ctenanthe.webp | ctenanthe | CC0 | File:Ctenanthe oppenheimiana - Shinjuku Gyo-en Greenhouse - DSC05761.jpg |

_Full source URLs (descriptionurl per file) are recorded in
`docs/sprints/feedback/PLANTPOTTING-0010/sourced-images.tsv`._

## PLANTPOTTING-0011 — botanical-plate → photograph swaps

A review of the whole library found 8 botanical-plate diagrams + 2 unrepresentative photos. With the
CC-BY relaxation (CC0/PD preferred, CC BY with attribution, then Unsplash License), **all 11 were
swapped to real photographs** — the last two (poinsettia, begonia, whose only Commons photos are
CC BY-SA) came from Unsplash.

| image | outcome | license |
|---|---|---|
| philodendron-pink-princess | **SWAPPED** — old plate was *P. erubescens* (green species), not the pink cultivar | CC0 (Cmushore) |
| monstera-adansonii | **SWAPPED** (was Curtis's Botanical Mag plate) → fenestrated-leaf photo | CC0 (Jacob Rehage) |
| saintpaulia-ionantha | **SWAPPED** (was Curtis' 1895 plate) → African-violet photo | CC0 (Daderot) |
| dionaea-muscipula | **SWAPPED** (was a painting) → potted Venus flytrap | CC0 (Deltapug) |
| chlorophytum-comosum | **SWAPPED** (was a seed on graph paper) → hanging spider plant | CC0 (W.carter) |
| chamaedorea-elegans | **SWAPPED** (was an illustration) → parlor-palm photo | Public domain (Biotaman) |
| dracaena (*D. marginata*) | **SWAPPED** (was Curtis 1808 plate) → dragon-tree photo | CC BY 3.0 (Forest & Kim Starr) |
| pachira-aquatica | **SWAPPED** (was Aublet 1775 plate) → money-tree photo | CC BY 4.0 (Atlas Þə Biologist) |
| anthurium-andraeanum | **SWAPPED** (was a spadix macro) → red flamingo-flower plant | CC BY 3.0 (Forest & Kim Starr) |
| euphorbia-pulcherrima (poinsettia) | **SWAPPED** (was Blanco plate) → red poinsettia photo | Unsplash License (Jessica Fadel) |
| begonia | **SWAPPED** (was Thornton plate) → potted begonia photo | Unsplash License (Sanni Sahil) |

The 3 CC BY swaps (dracaena, pachira, anthurium) are attributed in-app via `image_credits.tsv` on the
**Image credits** screen (reachable from Home → How it works).
