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

| Image file (`drawable-nodpi/`) | KB speciesId | License | Source (Unsplash / Wikimedia Commons / GBIF) |
|---|---|---|---|
| monstera_deliciosa.webp | monstera-deliciosa | Unsplash License | Unsplash — feey (https://unsplash.com/photos/green-plant-on-white-ceramic-pot-bwsTJMnhcwE) |
| monstera_adansonii.webp | monstera-adansonii | Unsplash License | Unsplash — Inna Safa (https://unsplash.com/photos/a-potted-plant-is-sitting-on-the-sidewalk-leuYRvpWVU0) |
| epipremnum_aureum.webp | epipremnum-aureum | Unsplash License | Unsplash — feey (https://unsplash.com/photos/gTMnUAkPvlQ) |
| spathiphyllum_wallisii.webp | spathiphyllum-wallisii | Unsplash License | Unsplash — feey (https://unsplash.com/photos/person-holding-white-ceramic-mug-with-green-plant-lmczPemWjQQ) |
| ficus_elastica.webp | ficus-elastica | Unsplash License | Unsplash — Elle Lumière (https://unsplash.com/photos/Dze_6fnPIKk) |
| ficus_lyrata.webp | ficus-lyrata | Unsplash License | Unsplash — Kara Eads (https://unsplash.com/photos/EbLX7oRo4vI) |
| dracaena_trifasciata.webp | dracaena-trifasciata | Unsplash License | Unsplash — Parker Sturdivant (https://unsplash.com/photos/a-potted-plant-on-a-wooden-table--Lh6tVzDdMg) |
| zamioculcas_zamiifolia.webp | zamioculcas-zamiifolia | Unsplash License | Unsplash — feey (https://unsplash.com/photos/green-plant-on-blue-ceramic-pot-cLaaxa4DSnc) |
| crassula_ovata.webp | crassula-ovata | Unsplash License | Unsplash — Susan Wilkinson (https://unsplash.com/photos/Q0w0LGkHokE) |
| chlorophytum_comosum.webp | chlorophytum-comosum | Unsplash License | Unsplash — feey (https://unsplash.com/photos/a-hand-holding-a-potted-plant-on-a-white-wall-RtH6rY9k738) |
| saintpaulia_ionantha.webp | saintpaulia-ionantha | Unsplash License | Unsplash — Sixteen Miles Out (https://unsplash.com/photos/590CL9EbVRo) |
| goeppertia_orbifolia.webp | goeppertia-orbifolia | Unsplash License | Unsplash — Gigi Visacri (https://unsplash.com/photos/a-green-plant-in-a-white-pot-on-a-table-rNjEk8d2vmQ) |
| hoya_carnosa.webp | hoya-carnosa | Unsplash License | Unsplash — Rebecca Matthews (https://unsplash.com/photos/variegated-hoya-plant-with-green-and-cream-leaves-74DjBL9SBGw) |
| aglaonema.webp | aglaonema | Unsplash License | Unsplash — feey (https://unsplash.com/photos/green-and-red-plant-on-brown-clay-pot-tSWTSzwSz6M) |
| anthurium_andraeanum.webp | anthurium-andraeanum | Unsplash License | Unsplash — Parker Sturdivant (https://unsplash.com/photos/EzRzvsY28Q0) |
| dieffenbachia.webp | dieffenbachia | Unsplash License | Unsplash — Vinicius Feiten (https://unsplash.com/photos/a-close-up-of-a-leaf-GVA-ckykAQM) |
| aloe_vera.webp | aloe-vera | Unsplash License | Unsplash — feey (https://unsplash.com/photos/xQKp9qCPAH0) |
| kalanchoe.webp | kalanchoe | Unsplash License | Unsplash — Mirella Callage (https://unsplash.com/photos/a-potted-plant-with-red-flowers-and-green-leaves-fDAUrBswpgM) |
| maranta_leuconeura.webp | maranta-leuconeura | Unsplash License | Unsplash — feey (https://unsplash.com/photos/a-person-holding-a-potted-plant-with-green-leaves-J-nfuMjUEH0) |
| nephrolepis_exaltata.webp | nephrolepis-exaltata | Unsplash License | Unsplash — Annie Knitter (https://unsplash.com/photos/a-potted-plant-hanging-on-a-wall-24N1YVZrUDk) |
| pachira_aquatica.webp | pachira-aquatica | Unsplash License | Unsplash — Jason Leung (https://unsplash.com/photos/a-potted-money-tree-against-an-orange-wall-BrbUdECo_5M) |
| dypsis_lutescens.webp | dypsis-lutescens | Unsplash License | Unsplash — feey (https://unsplash.com/photos/green-plant-on-gray-pot-yyRsvDp-zNc) |
| tradescantia.webp | tradescantia | Unsplash License | Unsplash — Adil Murshed (https://unsplash.com/photos/a-hanging-plant-with-deep-purple-leaves-indoors-g2qzCWhgYKE) |
| schefflera.webp | schefflera | Unsplash License | Unsplash — feey (https://unsplash.com/photos/green-plant-on-black-pot-F98esQWF6uI) |
| euphorbia_pulcherrima.webp | euphorbia-pulcherrima | Unsplash License | Unsplash — Daniel Cabanas (https://unsplash.com/photos/QqjzNZqz18g) |
| dionaea_muscipula.webp | dionaea-muscipula | Unsplash License | Unsplash — Sebastian Schuster (https://unsplash.com/photos/several-venus-flytrap-plants-with-open-traps-2SH2GmbZoJw) |
| chamaedorea_elegans.webp | chamaedorea-elegans | Unsplash License | Unsplash — Natalie Kinnear (https://unsplash.com/photos/Uii3CSyuItI) |
| strelitzia_reginae.webp | strelitzia-reginae | Unsplash License | Unsplash — Thimo van Leeuwen (https://unsplash.com/photos/yellow-and-blue-birds-of-paradise-flower-in-bloom-during-daytime-a4PRSLVQpEc) |
| aspidistra_elatior.webp | aspidistra-elatior | Unsplash License | Unsplash — Sascha Braun (https://unsplash.com/photos/GDo00rQ6fvg) |
| asparagus_setaceus.webp | asparagus-setaceus | Unsplash License | Unsplash — Ria Truter (https://unsplash.com/photos/a-close-up-of-a-green-plant-with-lots-of-leaves-BHJEh7RbfxU) |
| begonia.webp | begonia | Unsplash License | Unsplash — Sanni Sahil (https://unsplash.com/photos/KcUNr5_DK5A) |
| hypoestes_phyllostachya.webp | hypoestes-phyllostachya | Unsplash License | Unsplash — Parker Sturdivant (https://unsplash.com/photos/a-potted-plant-sitting-on-top-of-a-wooden-table-0aKF5P33CXQ) |
| beaucarnea_recurvata.webp | beaucarnea-recurvata | Unsplash License | Unsplash — feey (https://unsplash.com/photos/CU5E8ogHmuY) |
| schlumbergera_bridgesii.webp | schlumbergera-bridgesii | Unsplash License | Unsplash — Yoksel 🌿 Zok (https://unsplash.com/photos/bright-pink-christmas-cactus-flowers-bloom-beautifully-BT0m_0BwUkg) |
| philodendron_hederaceum.webp | philodendron-hederaceum | Unsplash License | Unsplash — Kevin Lessy (https://unsplash.com/photos/aKqw_M1CmfI) |
| philodendron_pink_princess.webp | philodendron-pink-princess | Unsplash License | Unsplash — feey (https://unsplash.com/photos/a-close-up-of-a-green-mountain-_FUKzwH1uo4) |
| phalaenopsis.webp | phalaenopsis | Unsplash License | Unsplash — Alexandra Nosova (https://unsplash.com/photos/white-moth-orchids-in-bloom-kly-z2c54b0) |
| alocasia.webp | alocasia | Unsplash License | Unsplash — Louis Hansel (https://unsplash.com/photos/shallow-focus-photo-of-green-indoor-plants-HGkn-eLCyOM) |
| dracaena.webp | dracaena | Unsplash License | Unsplash — François Giestas (https://unsplash.com/photos/a-close-up-of-a-plant-on-a-white-background-ZdIr7rpkr4k) |
| hedera_helix.webp | hedera-helix | Unsplash License | Unsplash — Ivana Djudic (https://unsplash.com/photos/shallow-focus-of-leaves-2rDC_qGWWM4) |
| asplenium_nidus.webp | asplenium-nidus | Unsplash License | Unsplash — feey (https://unsplash.com/photos/green-plant-on-brown-clay-pot-lxb3Azfrpqc) |
| cycas_revoluta.webp | cycas-revoluta | CC0 | GBIF/iNaturalist — Rosie Bibby (https://www.inaturalist.org/photos/471752890) |
| yucca.webp | yucca | Unsplash License | Unsplash — Anca Gabriela Zosin (https://unsplash.com/photos/f3e9p4_lqRw) |
| ctenanthe.webp | ctenanthe | CC BY 3.0 | File:Ctenanthe oppenheimiana.JPG (Oeropium) |

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

## PLANTPOTTING-0011 — hero-photo quality refresh

A follow-up pass re-sourced **29** result-screen hero photos to vetted, species-correct photographs —
preferring a whole potted/indoor plant on a clean background with the diagnostic feature visible. Each
candidate was visually vetted for species identity before acceptance. The replaced images:
`aglaonema, alocasia, asplenium_nidus, asparagus_setaceus, aspidistra_elatior,
philodendron_pink_princess, schefflera, schlumbergera_bridgesii, spathiphyllum_wallisii,
strelitzia_reginae, tradescantia, zamioculcas_zamiifolia, chlorophytum_comosum, ctenanthe,
cycas_revoluta, dieffenbachia, dionaea_muscipula, dracaena, dracaena_trifasciata,
goeppertia_orbifolia, hedera_helix, hoya_carnosa, hypoestes_phyllostachya, kalanchoe,
maranta_leuconeura, monstera_adansonii, monstera_deliciosa, nephrolepis_exaltata, pachira_aquatica`.

Most came from **Unsplash** (Unsplash License); `ctenanthe` is **CC BY 3.0** (Wikimedia Commons, attributed
in-app) and `cycas_revoluta` is **CC0** (GBIF/iNaturalist). Licenses + sources are reflected in the manifest
table above and `image_credits.tsv`; **no CC BY-SA** was used. `maranta_leuconeura` was additionally swapped
to fix a likely mis-ID (the old public-domain plate read as a *Calathea*, not a prayer plant).
