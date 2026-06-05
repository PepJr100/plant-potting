# Species targets — controlled vocabulary & disjoint-split exclusion set

**Gates the research (Phase 0 → Phase 1).** Every source query in `query-log.md` MUST use the
accepted scientific name and the listed synonyms from this file; counts gathered under any other
name are meaningless. Synonym handling is reconciled with `docs/kb/ml-mapping-notes.md`.

Source of truth for names: `app/src/main/assets/kb/species.json` (read-only this sprint).
Source of truth for the production model vocabulary: `app/src/main/assets/ml/house_plant_species_mobilenetv2/labels.csv` (47 classes).

## Why these 7 are the assessment surface

The production model has **47 classes** (`labels.csv`). Cross-checking the 6 OOV targets against
that list confirms **none** has a class:

| KB target | Nearest model class(es) | In model? |
|---|---|---|
| `monstera-adansonii` | `Monstera Deliciosa (Monstera deliciosa)` only | **OOV** — no adansonii class |
| `philodendron-hederaceum` | (no Philodendron class at all) | **OOV** |
| `philodendron-pink-princess` | (no Philodendron class at all) | **OOV** |
| `ficus-lyrata` | `Rubber Plant (Ficus elastica)` only | **OOV** — no lyrata class |
| `chlorophytum-comosum` | (no spider-plant class) | **OOV** |
| `hoya-carnosa` | (no Hoya class) | **OOV** |
| `epipremnum-aureum` ↔ Pilea | `Pothos (Ivy arum)` **and** `Chinese Money Plant (Pilea peperomioides)` | **both in model** (boundary, not OOV) |

So fine-tuning would **add/relabel a head class** for the 6 OOV species, and **add hard examples to
two existing classes** for the boundary pair.

## 6 out-of-vocab (OOV) targets

### 1. `monstera-adansonii`
- **Accepted name:** *Monstera adansonii* Schott
- **Common names:** Swiss cheese vine, Monkey mask, Five holes plant
- **Synonyms / trade names to also query:** *Monstera friedrichsthalii* (common trade synonym)
- **Lookalike / mislabel risk:** in-vocab `monstera-deliciosa` (fenestration differs — adansonii leaves are entirely enclosed-hole, deliciosa leaves are edge-split); also frequently **mislabelled as the rare *Monstera obliqua***, which inflates "adansonii" tags with the wrong plant and vice-versa. Pitfall: confirm counts are not dominated by deliciosa or obliqua mislabels.

### 2. `philodendron-hederaceum`
- **Accepted name:** *Philodendron hederaceum* (Jacq.) Schott
- **Common names:** Heartleaf philodendron, Sweetheart plant
- **Synonyms / trade names to also query:** *Philodendron scandens*, *Philodendron oxycardium*, *Philodendron cordatum* (older horticultural names per `ml-mapping-notes.md`)
- **Lookalike / mislabel risk:** the **canonical pothos lookalike** — routinely **cross-tagged with `epipremnum-aureum`** (heart-shaped leaves, similar trailing habit; pothos has glossier, often variegated leaves, hederaceum is matte and thinner). Pitfall: record how much of each count is likely the other.

### 3. `philodendron-pink-princess`  *(highest-risk target)*
- **Accepted name:** *Philodendron erubescens* 'Pink Princess' — a **specific cultivar**
- **Common names:** Pink Princess philodendron, PPP
- **Query rule:** count the **pink-variegated cultivar specifically** (`Philodendron erubescens 'Pink Princess'`). **Generic *Philodendron erubescens* is EXCLUDED** from the recommendation — record a separate `cultivar-proven` count.
- **Lookalike / mislabel risk:** 'Pink Congo' (chemically-induced fake variegation — looks similar but is a different/fraudulent plant), reverted all-green *erubescens*, and other variegated philodendrons (e.g. 'White Knight'). A generic *erubescens* photo does **not** teach the pink variegation.

### 4. `ficus-lyrata`
- **Accepted name:** *Ficus lyrata* Warb.
- **Common names:** Fiddle-leaf fig, Banjo fig
- **Synonyms / trade names:** *Ficus pandurata* (older horticultural name)
- **Lookalike / mislabel risk:** low (distinctive lyre-shaped leaves). Pitfall: note **indoor/compact bush-form vs mature tree-form** imagery — outdoor tree shots of mature *F. lyrata* may not represent the potted houseplant the app targets.

### 5. `chlorophytum-comosum`
- **Accepted name:** *Chlorophytum comosum* (Thunb.) Jacques
- **Common names:** Spider plant, Airplane plant, Ribbon plant, Hen-and-chickens
- **Synonyms / trade names:** cultivars 'Vittatum', 'Variegatum', 'Bonnie'
- **Lookalike / mislabel risk:** variegated vs all-green forms; note the cultivar split. Low cross-species mislabel risk (distinctive arching variegated strap leaves + plantlets).

### 6. `hoya-carnosa`
- **Accepted name:** *Hoya carnosa* (L.f.) R.Br.
- **Common names:** Wax plant, Porcelain flower, Honey plant
- **Synonyms / trade names:** *Asclepias carnosa* (basionym)
- **Lookalike / mislabel risk:** cultivars 'Compacta' (Hindu rope), 'Variegata', 'Krimson Queen', 'Krimson Princess', 'Tricolor' — these **may not represent the base species cleanly** (twisted/variegated leaves). Also confusable with other *Hoya* species (e.g. *H. kerrii*, *H. pubicalyx*). Pitfall: note how much of the count is base-form vs cultivar.

## Boundary problem (both classes in the model, mutually confused)

### 7. `epipremnum-aureum` ↔ Pilea
- **Pothos accepted name:** *Epipremnum aureum* (Linden & André) G.S.Bunting
  - **Common names:** Pothos, Golden pothos, Devil's ivy, Money plant
  - **Synonyms / trade names to also query:** *Epipremnum pinnatum* 'Aureum', *Scindapsus aureus*, *Pothos aureus*, *Rhaphidophora aurea*
  - **EXCLUDE:** true ***Pothos*-genus** records (*Pothos scandens* etc. — a different aroid genus); the trade "pothos" is *Epipremnum aureum*, not genus *Pothos*.
- **Pilea accepted name:** *Pilea peperomioides* Diels — **the production model's `Chinese Money Plant (Pilea peperomioides)` class specifically**
  - **Common names:** Chinese money plant, Pancake plant, UFO plant, Missionary plant
  - **EXCLUDE:** generic genus *Pilea* (e.g. *P. cadierei* aluminium plant, *P. involucrata*) — assess only *P. peperomioides*, the actual class.
- **Assessment rule:** count varied **hard-example** imagery for **EACH side separately** (trailing
  vines, juvenile leaves, confusable leaf shapes) toward the ~150–300-each boundary target. This is
  NOT a new class — it adds hard examples to two existing classes to break the
  pothos→Pilea misclassification.

## Disjoint-split exclusion set (the 8 existing test fixtures)

From `app/src/androidTest/assets/identify-fixtures/` (URLs from its `LICENSE.txt`). The future
fine-tune's train/val/test splits MUST exclude these source files so the held-out eval fixtures stay
disjoint from training data.

| Fixture file | Depicts | Wikimedia source URL (the file to exclude) |
|---|---|---|
| `crassula-ovata.jpg` | *Crassula ovata* | https://commons.wikimedia.org/wiki/File:Jade_Plant,_Crassula_ovata_IMG_3632.jpg |
| `dracaena-trifasciata.jpg` | *Dracaena trifasciata* | https://commons.wikimedia.org/wiki/File:Snake_plant_(Sansevieria_trifasciata)_Waoleona_Buton_Island_01.jpg |
| **`epipremnum-aureum.jpg`** | *Epipremnum aureum* (pothos) | **https://commons.wikimedia.org/wiki/File:Golden_Pothos_(Money_Plant).jpg** ← **MUST be excluded from any pothos count** (overlaps the boundary target) |
| `goeppertia-orbifolia.jpg` | *Goeppertia orbifolia* | https://commons.wikimedia.org/wiki/File:Prayer_plant_(Calathea_orbifolia).jpg |
| `monstera-deliciosa.jpg` | *Monstera deliciosa* | https://commons.wikimedia.org/wiki/File:HK_SW_Leaves_with_holes.JPG |
| `phalaenopsis.jpg` | *Phalaenopsis* | https://commons.wikimedia.org/wiki/File:Phalaenopsis_orchid_plant.jpeg |
| `spathiphyllum-wallisii.jpg` | *Spathiphyllum wallisii* | https://commons.wikimedia.org/wiki/File:Spathiphyllum_Wallisii_-_Peace_Lily_Plant_at_Wayanad_(3).jpg |
| `zamioculcas-zamiifolia.jpg` | *Zamioculcas zamiifolia* | https://commons.wikimedia.org/wiki/File:%22Zamio_Mater%22_Zamioculcas_zamiifolia_communis_plant.jpg |

**Only `epipremnum-aureum.jpg` overlaps a target species** (the boundary pothos). The other 7 depict
non-target species; they are recorded for completeness but do not constrain the OOV-target counts.
The exclusion is by **source observation/file id**, applied at download time in the future sprint.
