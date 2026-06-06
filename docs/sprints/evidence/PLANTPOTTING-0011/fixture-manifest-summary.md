# PLANTPOTTING-0011 — fixture sourcing summary (scarcity log)

Honest record of the CC0/PD-only eval-fixture sourcing (no self-shot allowed; CC-BY/CC-BY-SA not
accepted for new fixtures). Candidates were searched on Wikimedia Commons via
`scripts/source-identify-fixtures.ps1`, then **manually vetted** (each candidate viewed) before
acceptance — a wrong/cluttered photo would poison the scorecard. No silent skips.

## Added this sprint (8 new CC0/PD fixtures, 480×480 JPEG q80)

| fixture | species | license | author | note |
|---|---|---|---|---|
| `dracaena-trifasciata__01.jpg` | snake plant | Public domain | Tangopaso | 2nd base photo (low hahnii form) |
| `dracaena-trifasciata__02.jpg` | snake plant | Public domain | Mokkie | 3rd base photo (tall green form) |
| `ficus-elastica__01.jpg` | rubber plant | CC0 | Al Kordesch | only clean option; outdoor/cluttered framing |
| `aloe-vera__01.jpg` | aloe vera | Public domain | Arjun01 | clean potted single subject |
| `aglaonema__01.jpg` | Chinese evergreen | CC0 | Satirdan kahraman | vivid red cultivar cluster |
| `dieffenbachia__01.jpg` | dumb cane | CC0 | Alex Rio Brazil | textbook variegated leaves |
| `schefflera__01.jpg` | umbrella tree | CC0 | Cbaile19 | bonsai form, palmate leaves |
| `kalanchoe__01.jpg` | kalanchoe | CC0 | Sabalo22 | flowering, orange clusters |

The **snake plant** (the 0010-review priority failure) now has **3 independent base photos**
(existing `dracaena-trifasciata.jpg` + `__01` + `__02`) — the strongest defence against overfitting
a tiny clean set (D2).

## Could NOT be sourced cleanly (logged, not silently dropped)

| target species | why no new CC0/PD fixture |
|---|---|
| `epipremnum-aureum` (pothos, priority) | Top CC0/PD hits were a botanical **illustration** (L'Illustration Horticole), the **same** image already bundled as the existing fixture (a duplicate), and a different species (*E. pinnatum*). Kept the existing single fixture; no clean 2nd base photo. |
| `euphorbia-pulcherrima` (poinsettia) | Only CC0/PD results were **illustrations / a 1904 art print** (PSF line art, Brück & Sohn Kunstverlag) — no real photograph. |
| `anthurium-andraeanum` | Only CC0/PD results were an extreme **spadix macro** and a **fruit** close-up — neither represents the whole flamingo-flower plant. |
| `hedera-helix` (English ivy) | **No CC0/PD** result among the top 40 Commons matches (all CC-BY / CC-BY-SA / GFDL). |
| `dracaena` (*D. marginata*) | **No CC0/PD** result among the top 40 Commons matches. |

## Second sourcing pass (deepen single-photo species toward ≥2)

Added 4 more CC0 photos, manually vetted — species now with **≥2 independent base photos**:
snake plant (3), aloe vera (2), jade (2), ZZ plant (2), peace lily (2, incl. the grandfathered CC BY-SA).

| added | species | license | author |
|---|---|---|---|
| `zamioculcas-zamiifolia__01.jpg` | ZZ plant | CC0 | Philsacor |
| `aloe-vera__02.jpg` | aloe vera | CC0 | Brainmachine |
| `crassula-ovata__01.jpg` | jade plant | CC0 | W.carter |
| `spathiphyllum-wallisii__01.jpg` | peace lily | CC0 | CesarAlbertoHerrera |

Wikimedia hit a supply wall for several species; resolved in pass 3 below via GBIF + Smithsonian.

## Third sourcing pass (GBIF/iNaturalist + Smithsonian Gardens, CC0)

Wikimedia Commons isn't the only CC0 source. **GBIF** (federates iNaturalist) and **Smithsonian Open
Access** (api.data.gov key) were searched, **filtered per-media to CC0**, and manually vetted (many GBIF
"botany" CC0 images are herbarium specimen sheets — rejected on sight).

| added | species | source | author |
|---|---|---|---|
| `epipremnum-aureum__01.jpg` | pothos (2nd) | GBIF/iNaturalist | 葉子 |
| `monstera-deliciosa__01.jpg` | Monstera (2nd) | GBIF/iNaturalist | Frank Thomas Sautter |
| `ficus-elastica__02.jpg` | rubber plant (3rd) | GBIF/iNaturalist | 葉子 |
| `kalanchoe__02.jpg` | kalanchoe (2nd) | GBIF/iNaturalist | Arne Holgersson |
| `phalaenopsis__01.jpg` | moth orchid (2nd) | **Smithsonian Gardens** (Orchid Collection, OFEO-SG) | Creekside Digital |

Key finding: **Smithsonian NMNH _Botany_ = herbarium specimen sheets** (not usable), but **Smithsonian
_Gardens_ (unit OFEO-SG) = live-plant studio photos** (CC0) — that's where the Phalaenopsis came from
(GBIF had no CC0 phalaenopsis). The Gardens collection is orchid-heavy, so it yielded only phalaenopsis.

**Still at 1 photo (no clean CC0 live-plant photo found anywhere):** `aglaonema`, `dieffenbachia`,
`schefflera`, `goeppertia-orbifolia`. **Net fixture set: 25 photos across 14 species** — 10 of 14 species
now have ≥2 independent base photos (snake ×3, ficus ×3; aloe/jade/ZZ/peace-lily/monstera/pothos/
kalanchoe/phalaenopsis ×2).

## Method notes

- Center-crop to square → resize 480×480 → JPEG q80 (matches the pre-existing fixtures exactly).
- License/author/URL captured per file in `app/src/androidTest/assets/identify-fixtures/fixture-manifest.tsv`
  and cross-checked by `FixtureLicenseManifestTest` (CC0/PD enforced for all new fixtures).
- 4 pre-0011 fixtures are CC BY-SA and **grandfathered** (incl. the AIY anchor `monstera-deliciosa.jpg`,
  which must not be swapped); the cross-check forbids adding any further non-CC0/PD fixture.
- **Limitation (load-bearing):** this clean set is small and under-represents the principal's messy
  real-world captures. Scorecard numbers are *measured-under-clean-CC-conditions + synthetic-robustness*,
  NOT a claim that real-world accuracy is solved.
