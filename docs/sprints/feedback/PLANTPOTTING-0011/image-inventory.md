# PLANTPOTTING-0011 — image inventory (all three folders)

For your diagram cleanup. Three separate image sets — **the diagrams are all in the reference-image
folder** (folder 2); the eval fixtures (folder 1) are all real photos. ✅ keep · ❌ diagram/plate
(delete + replace) · ⚠ real photo but unrepresentative (replace).

---

## Folder 1 — EVAL FIXTURES (test-only ruler, NOT shipped)
`app/src/androidTest/assets/identify-fixtures/` — 16 files, **all real photos** (each manually vetted)

aglaonema__01.jpg · aloe-vera__01.jpg · crassula-ovata.jpg · dieffenbachia__01.jpg ·
dracaena-trifasciata.jpg · dracaena-trifasciata__01.jpg · dracaena-trifasciata__02.jpg ·
epipremnum-aureum.jpg · ficus-elastica__01.jpg · goeppertia-orbifolia.jpg · kalanchoe__01.jpg ·
monstera-deliciosa.jpg · phalaenopsis.jpg · schefflera__01.jpg · spathiphyllum-wallisii.jpg ·
zamioculcas-zamiifolia.jpg

→ nothing to delete here.

---

## Folder 2 — REFERENCE IMAGES (shipped in the app, shown on the result screen)
`app/src/main/res/drawable-nodpi/` — 44 `.webp`, one per species

### ❌ Diagrams / botanical plates — DELETE + REPLACE (8)
| file | what it is | CC0/PD photo available? |
|---|---|---|
| `monstera_adansonii.webp` | Curtis's Botanical Magazine plate (.png) | needs search |
| `saintpaulia_ionantha.webp` | Curtis' 1895 plate | needs search |
| `pachira_aquatica.webp` | Aublet 1775 plate | needs search |
| `euphorbia_pulcherrima.webp` | Blanco plate (poinsettia) | **NO** — only plates/art exist |
| `dionaea_muscipula.webp` | Hist. Pl. Bresil plate | needs search |
| `chamaedorea_elegans.webp` | illustration (.png), parlor palm | **NO** — only plates exist |
| `dracaena.webp` | Curtis 1808 plate (*D. marginata*) | **NO** — none in top 40 |
| `begonia.webp` | Thornton painterly plate (painted butterfly) | needs search |

### ⚠ Real photo but unrepresentative — REPLACE (2)
| file | what it is |
|---|---|
| `chlorophytum_comosum.webp` | a seed + plantlet on graph paper — doesn't show a spider plant |
| `anthurium_andraeanum.webp` | extreme macro of the spadix only (borderline; your call) |

### ✅ Real, representative photos — KEEP (34)
aglaonema · alocasia · aloe_vera · asparagus_setaceus · aspidistra_elatior · asplenium_nidus ·
beaucarnea_recurvata · crassula_ovata · ctenanthe · cycas_revoluta · dieffenbachia · dypsis_lutescens ·
epipremnum_aureum · ficus_elastica · ficus_lyrata · goeppertia_orbifolia · hedera_helix · hoya_carnosa ·
hypoestes_phyllostachya · kalanchoe · maranta_leuconeura · monstera_deliciosa · nephrolepis_exaltata ·
philodendron_hederaceum · philodendron_pink_princess (just swapped to a CC0 photo) · phalaenopsis ·
schefflera · schlumbergera_bridgesii · spathiphyllum_wallisii · strelitzia_reginae · tradescantia ·
yucca · zamioculcas_zamiifolia · dracaena_trifasciata

**Heads-up:** 3 of the 8 plates (poinsettia, parlor palm, *Dracaena marginata*) have **no CC0/PD
photograph** I could find — deleting those means either relaxing the licence rule (CC-BY) or falling
back to the authored placeholder vector until a clean photo appears. The other 5 I'll try to source.

---

## Folder 3 — CANDIDATE POOL (disposable, gitignored temp)
`app/build/pp-fixture-src/` — 26 downloaded candidates incl. the ones I **rejected** during vetting
(illustrations, fruit/spadix macros, duplicates). Not in the repo; safe to ignore/delete entirely.
