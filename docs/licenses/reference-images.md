# Reference image attribution manifest

PLANTPOTTING-0010 Phase 6 / D4. Every bundled reference image under
`app/src/main/res/drawable-nodpi/*.webp` **must** have a row in the table below. License policy below
(CC0/PD preferred; CC BY + Unsplash License allowed; **no CC BY-SA**). CI-enforced by
`ReferenceImageManifestTest` + `ImageCreditsTest`.

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
| monstera_deliciosa.webp | monstera-deliciosa | Unsplash License | Unsplash — Natalie Kovach (https://unsplash.com/photos/ph7QQq63lCs) |
| monstera_adansonii.webp | monstera-adansonii | CC0 | File:Monstera adansonii 112059105.jpg (Jacob Rehage) |
| epipremnum_aureum.webp | epipremnum-aureum | Unsplash License | Unsplash — feey (https://unsplash.com/photos/gTMnUAkPvlQ) |
| spathiphyllum_wallisii.webp | spathiphyllum-wallisii | Unsplash License | Unsplash — Maria Elizabeth (https://unsplash.com/photos/CDoPIWJDvvw) |
| ficus_elastica.webp | ficus-elastica | Unsplash License | Unsplash — Elle Lumière (https://unsplash.com/photos/Dze_6fnPIKk) |
| ficus_lyrata.webp | ficus-lyrata | Unsplash License | Unsplash — Kara Eads (https://unsplash.com/photos/EbLX7oRo4vI) |
| dracaena_trifasciata.webp | dracaena-trifasciata | CC0 | File:Snake plant (Sansevieria trifasciata) Waoleona Buton Island 02.jpg |
| zamioculcas_zamiifolia.webp | zamioculcas-zamiifolia | CC0 | File:Millonaria (Zamioculcas zamiifolia).jpg |
| crassula_ovata.webp | crassula-ovata | Unsplash License | Unsplash — Susan Wilkinson (https://unsplash.com/photos/Q0w0LGkHokE) |
| chlorophytum_comosum.webp | chlorophytum-comosum | CC0 | File:Spider plant with plantlets and flowers.jpg (W.carter) |
| saintpaulia_ionantha.webp | saintpaulia-ionantha | Unsplash License | Unsplash — Sixteen Miles Out (https://unsplash.com/photos/590CL9EbVRo) |
| goeppertia_orbifolia.webp | goeppertia-orbifolia | CC0 | File:Calathea orbiculata - Wellington Botanic Garden - DSC09392.jpg |
| hoya_carnosa.webp | hoya-carnosa | Unsplash License | Unsplash — Rebecca Matthews (https://unsplash.com/photos/99vAqGuu1i4) |
| aglaonema.webp | aglaonema | CC0 | File:Konya Kelebekler Vadisi Aglaonema.jpg |
| anthurium_andraeanum.webp | anthurium-andraeanum | Unsplash License | Unsplash — Parker Sturdivant (https://unsplash.com/photos/EzRzvsY28Q0) |
| dieffenbachia.webp | dieffenbachia | CC0 | File:Dieffenbachia moralis holotype 01.jpg |
| aloe_vera.webp | aloe-vera | Unsplash License | Unsplash — feey (https://unsplash.com/photos/xQKp9qCPAH0) |
| kalanchoe.webp | kalanchoe | CC0 | File:Kalanchoe blossfeldiana (Florist Kalanchoe).jpg |
| maranta_leuconeura.webp | maranta-leuconeura | Public domain | File:Maranta leuconeura D2411.jpg |
| nephrolepis_exaltata.webp | nephrolepis-exaltata | Unsplash License | Unsplash — Anna Zaro (https://unsplash.com/photos/XNdOjApFA04) |
| pachira_aquatica.webp | pachira-aquatica | Unsplash License | Unsplash — Junpeng Ouyang (https://unsplash.com/photos/o3wlEHi33QA) |
| dypsis_lutescens.webp | dypsis-lutescens | Unsplash License | Unsplash — feey (https://unsplash.com/photos/green-plant-on-gray-pot-yyRsvDp-zNc) |
| tradescantia.webp | tradescantia | Unsplash License | Unsplash — Uliana Semenova (https://unsplash.com/photos/KBKho0S45aQ) |
| schefflera.webp | schefflera | CC0 | File:Schefflera arboricola (bonsai), Phipps Conservatory, 2014-03-01.jpg |
| euphorbia_pulcherrima.webp | euphorbia-pulcherrima | Unsplash License | Unsplash — Daniel Cabanas (https://unsplash.com/photos/QqjzNZqz18g) |
| dionaea_muscipula.webp | dionaea-muscipula | Unsplash License | Unsplash — Andi Superkern (https://unsplash.com/photos/7SeRQRY_g0k) |
| chamaedorea_elegans.webp | chamaedorea-elegans | Unsplash License | Unsplash — Natalie Kinnear (https://unsplash.com/photos/Uii3CSyuItI) |
| strelitzia_reginae.webp | strelitzia-reginae | Unsplash License | Unsplash — LARAM (https://unsplash.com/photos/WhxGVPus9JM) |
| aspidistra_elatior.webp | aspidistra-elatior | Unsplash License | Unsplash — pawel blazewicz (https://unsplash.com/photos/bR9hMgBoKP0) |
| asparagus_setaceus.webp | asparagus-setaceus | Unsplash License | Unsplash — Ria Truter (https://unsplash.com/photos/BHJEh7RbfxU) |
| begonia.webp | begonia | Unsplash License | Unsplash — Sanni Sahil (https://unsplash.com/photos/KcUNr5_DK5A) |
| hypoestes_phyllostachya.webp | hypoestes-phyllostachya | Public domain | File:2006 08 14 Hypoestes Phyllostachya.jpg |
| beaucarnea_recurvata.webp | beaucarnea-recurvata | Unsplash License | Unsplash — feey (https://unsplash.com/photos/CU5E8ogHmuY) |
| schlumbergera_bridgesii.webp | schlumbergera-bridgesii | CC0 | File:Christmas Cactus October 2022.jpg |
| philodendron_hederaceum.webp | philodendron-hederaceum | Unsplash License | Unsplash — Kevin Lessy (https://unsplash.com/photos/aKqw_M1CmfI) |
| philodendron_pink_princess.webp | philodendron-pink-princess | CC0 | File:Pink princess philodendron.jpg (Cmushore) |
| phalaenopsis.webp | phalaenopsis | Unsplash License | Unsplash — Alexandra Nosova (https://unsplash.com/photos/white-moth-orchids-in-bloom-kly-z2c54b0) |
| alocasia.webp | alocasia | CC0 | File:Alocasia macrorrhizos 'Nigra' 02.jpg |
| dracaena.webp | dracaena | Unsplash License | Unsplash — Ahail Das (https://unsplash.com/photos/GJenoZICKxs) |
| hedera_helix.webp | hedera-helix | Public domain | File:Hedera helix lombozata.jpg |
| asplenium_nidus.webp | asplenium-nidus | Public domain | File:Neottopteris nidus - Kunming Botanical Garden - DSC03126.JPG |
| cycas_revoluta.webp | cycas-revoluta | Public domain | File:Center of sago palm (Cycas revoluta) close-up.jpg |
| yucca.webp | yucca | Unsplash License | Unsplash — Anca Gabriela Zosin (https://unsplash.com/photos/f3e9p4_lqRw) |
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
