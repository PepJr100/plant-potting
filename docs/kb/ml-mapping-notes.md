# ML Label → KB Species Mapping Notes

Editorial companion to `app/src/main/assets/ml/aiy_plants_v1/plant_class_map.json`. One short paragraph per mapping line justifying the model-label → KB-species-id decision. The same editorial discipline as `plant-substrate-kb-notes.md`.

The mapping is the single source-of-truth for resolving an AIY Plants V1 model prediction back to a KB species id. Aliases (`alias: true`) are model labels that resolve to the same KB id as a non-alias counterpart — these capture pre-Byng et al. 2018 nomenclature still appearing in retail tags and (likely) in the AIY V1 label vocabulary.

---

### `Monstera deliciosa` → `monstera-deliciosa`
Direct rank-match. The AIY V1 vocabulary is binomial at the species rank, and the KB key already lives at that rank.

### `Monstera adansonii` → `monstera-adansonii`
Direct rank-match. Monstera adansonii is a sufficiently distinct species from M. deliciosa (smaller leaves, more fenestrated, more thoroughly epiphytic) that the model should resolve them as separate classes.

### `Epipremnum aureum` → `epipremnum-aureum`
Direct rank-match. The trade-name chaos (pothos / devil's ivy / golden pothos / occasional Scindapsus confusion) is upstream of the model — by the time AIY V1 emits `Epipremnum aureum`, we trust the genus is correct.

### `Philodendron hederaceum` → `philodendron-hederaceum`
Direct rank-match under the *current* name. Older horticultural references still use `Philodendron scandens` / `P. oxycardium`; if AIY V1 happens to surface either of those as the canonical label, this mapping must be widened with explicit aliases. Today we only map the modern name.

### `Philodendron erubescens` → `philodendron-pink-princess`
Cross-rank mapping. The AIY V1 vocabulary is species-rank; the KB species id encodes the popular cultivar (`'Pink Princess'`) because that's the retail SKU users actually buy. Substrate recommendation is identical for the cultivar and the wild type (both want the aroid-chunky archetype), so collapsing the model's species-rank `Philodendron erubescens` onto the cultivar id is editorially safe. If a future sprint adds the green-leaved wild type as a separate KB species, this mapping must split.

### `Spathiphyllum wallisii` → `spathiphyllum-wallisii`
Direct rank-match. Peace lilies sold in the West are almost entirely *S. wallisii* hybrids; we accept the model's call.

### `Ficus lyrata` → `ficus-lyrata`
Direct rank-match. The fiddle-leaf fig's silhouette and leaf venation are distinctive enough that the model should rarely confuse it with other Ficus.

### `Ficus elastica` → `ficus-elastica`
Direct rank-match. The rubber plant shares a genus with *F. lyrata* but is distinguishable; substrate envelope differs subtly per the KB notes.

### `Sansevieria trifasciata` → `dracaena-trifasciata` *(alias)*
Cross-name mapping. Byng et al. 2018 transferred Sansevieria into Dracaena, but the retail trade and many ML label sets predate this. If AIY V1's vocabulary still has the pre-2018 name, we route it to the modern KB id. The non-alias counterpart `Dracaena trifasciata` covers the case where the model has been updated.

### `Dracaena trifasciata` → `dracaena-trifasciata`
Direct rank-match under the *current* name. Companion to the Sansevieria alias above.

### `Zamioculcas zamiifolia` → `zamioculcas-zamiifolia`
Direct rank-match. The ZZ plant is a monotypic-feeling genus in retail (no other Zamioculcas commonly sold), so the model's species-rank label resolves unambiguously.

### `Chlorophytum comosum` → `chlorophytum-comosum`
Direct rank-match. Spider plant varieties are visually similar enough at the leaf-shape level that the model is unlikely to discriminate cultivars; species-rank is the right resolution.

### `Phalaenopsis` → `phalaenopsis`
Genus-rank mapping. Retail moth orchids are uniformly hybrid grex plants; species-rank resolution would be misleading. The KB key is already at the genus rank by design (see `species.json` notes), and the AIY V1 vocabulary likely keeps it at the genus rank as well — this is the rare alignment where genus is the correct resolution.

### `Calathea orbifolia` → `goeppertia-orbifolia` *(alias)*
Cross-name mapping. The Calathea-to-Goeppertia transfer (Borchsenius et al.) is recent enough that most retail tags and many label sets still use the Calathea name. We honour both with the alias mechanism.

### `Goeppertia orbifolia` → `goeppertia-orbifolia`
Direct rank-match under the *current* name. Companion to the Calathea alias above.

### `Crassula ovata` → `crassula-ovata`
Direct rank-match. Jade plant cultivars are visually similar; species-rank is fine. The substrate envelope (succulent-gritty) is identical across cultivars.

### `Saintpaulia ionantha` → `saintpaulia-ionantha`
Direct rank-match. African violet hybrids are sold under the species name even when they are interspecific crosses; we follow the trade convention.

### `Hoya carnosa` → `hoya-carnosa`
Direct rank-match. Hoya carnosa is the most widely sold Hoya in the West; we accept the model's species call. The KB recipe is a blend (semi-hydro-inert + epiphytic-orchid-bark), which the recommendation engine handles transparently — the mapping does not need to reflect the blend structure.

---

## Coverage notes

- The current `plant_class_map.json` contains **18 mappings** for the **16 KB species** (two species — Dracaena trifasciata and Goeppertia orbifolia — have alias entries for their pre-transfer scientific names).
- Every KB species id appears as a `kbSpeciesId` value at least once. This is enforced by `ModelLabelMappingValidationTest.mappingCoversEveryBundledKbSpecies`.
- The real AIY V1 vocabulary (extracted from the upstream `.tflite` as `probability-labels-en.txt`, 2101 scientific names) overlaps our retail-houseplant KB on just **2 of 18 mapping keys**: `Monstera deliciosa` and `Crassula ovata`. The remaining 14 species are not in the model's training set at the species rank, so high-confidence direct hits for them will never fire. This matches the §7.1 risk assessment, and the design's intended safety net is `LowConfidencePicker`'s manual search — every KB species is reachable from there.
- The dormant 14 mapping entries are intentional editorial intent. They survive a future model variant that adds the species (e.g., a fine-tuned retail-focused checkpoint), and they document that we *would* map those labels if they appeared. `ModelLabelMappingValidationTest.mappingHasAtLeastOneKeyInUpstreamLabels` enforces the minimum-viable invariant (at least one mapping key actually matches the vocabulary, so at least one high-confidence path is reachable).

## When to update this file

- A new model variant lands — re-audit each mapping line against the new vocabulary; some labels may have been renamed.
- A KB species is added or renamed in `species.json` — add a mapping entry pointing at the new id and a paragraph here.
- A KB species id changes (rare; we avoid this) — propagate to every mapping row pointing at the old id, and either rewrite the paragraph or delete the row.

## Calibration provenance

PLANTPOTTING-0005 §5 introduced the *per-species threshold override* mechanism — a new
`perSpeciesThresholds: Map<String, Float>` field on `ModelManifest`, parsed from
`per_species_thresholds` in `model_manifest.json`, and consulted by `ModelScoreMapper`
*before* the global `Thresholds.highConfidencePlain` default. The override applies only
to the `_plain` (top-1 vs threshold) path; the margin path (`_margin_min` / `_margin_delta`)
is intentionally not overridable — see PLANTPOTTING-0005 §4.3 (premature surface).

**Probe outcome.** Run on the `pixel6Api34` GMD against the real CC-licensed
Monstera deliciosa fixture (§5.4). Measured: top-1 = `monstera-deliciosa` at
p = **0.8984**, `lowConfidence = false`, `source = ON_DEVICE_MODEL`; the only other
mapped candidate was `crassula-ovata` at p ≈ 0.0000 (the AIY V1/3 vocabulary maps
just these two KB species). The top-1 probability clears the global
`high_confidence_plain = 0.55` threshold cleanly (+0.35 margin), so the photo routes
**high-confidence direct** to Monstera. Per §5.6 this selects the *preferred*
assertion form (`speciesId == "monstera-deliciosa" && !lowConfidence`), now committed
in `OnDeviceModelRealInterpreterTest.realMonsteraPhotoRoutesHighConfidenceToMonstera`.

**Seeded values.** None — and correctly so. §5.7 seeds a per-species override only
when an in-vocab species *fails* the global threshold by a closeable margin. Monstera
clears 0.55 outright (0.8984), so `per_species_thresholds` ships as an empty object;
seeding it would be anti-overfitting (§5.6 prohibition). The multi-species calibration
sweep remains PLANTPOTTING-0006's to own if the data ever forces per-class tuning.

### PLANTPOTTING-0006 — `crassula-ovata` probe (second & final in-vocab overlap)

**Probe outcome.** _(pending GMD run — Phase 2 will fill in measured top-1 id + p,
lowConfidence, source, other mapped candidates, route taken.)_

**Seeding decision.** _(pending — Phase 3 will record seeded-with-value or
not-warranted, with the deciding number.)_

**Conditional cleanups (gated — log if not forced).**

- *`perSpeciesThresholds` margin / lower-ranked-candidate semantics (Phase 3 conditional).*
  _(pending — revisited only if real crassula seeding forces it; otherwise logged here as an
  open question, not built.)_
- *Splitting `FakeFixedIdentifier` into focused fakes (Phase 5 conditional).* **Not forced.**
  The §5 `(0%)`-chip fix is presentational only — a single branch in
  `LowConfidencePickerScreen.kt`'s chip-text builder (`probabilityPct > 0`). It added no new
  knob or interface to `FakeFixedIdentifier` (still 5 ctor params + 2 interfaces), so the
  fake was left intact per the gate. Deferred.
