# Knowledge Base Sourcing Notes — PLANTPOTTING-0001

Editorial source-of-truth for `app/src/main/assets/kb/archetypes.json` and
`app/src/main/assets/kb/species.json`. Every JSON `citations` entry points
back to a heading or table named below.

Source files (citation handles used in JSON):
- `docs/Research_brief.md` — the project brief; cite as `Brief §<section>`.
- `docs/research/deep-research-Gemini.md` — cite as `Gemini-report:<heading>`.
- `docs/research/houseplant_app_research_package_Claude.md` — cite as
  `Claude-report:<heading>`.

The eight substrate archetypes and the sixteen-species starter set are
*not* drawn from these notes; they are mandated verbatim by
`docs/sprints/PLANTPOTTING-0001.md` §4.1–§4.2. This file justifies and
sources those mandated choices.

---

## 1. Substrate archetypes — per-archetype sourcing

### 1.1 `standard-houseplant` — coir 60 / perlite 30 / bark fines 10

The default mix for general foliage plants with terrestrial fibrous root
systems. The recipe matches the "Standard houseplant" row of Brief §4.4
table (peat-or-coir 60% / perlite 30% / bark fines 10%). Coir is chosen
over peat to align with the 2026 transition away from peat-based media
described in Brief §4.5 ("Sustainability") and corroborated by the
Gemini report section *Regulatory and Sustainability Frameworks in 2026*
which documents the RHS "no new peat" policy effective 1 January 2026.
Coir's chemistry (CEC, pH 5.5–6.8, slow decomposition) is documented in
the Gemini report's *Substrate Component Analysis* table.

Citations:
- `Brief §4.4` (archetype recipe)
- `Brief §4.5` (peat phase-out — justifies coir over peat)
- `Gemini-report:Substrate Component Analysis and Transition to Peat-Free Media`

### 1.2 `aroid-chunky` — pine/orchid bark 40 / coco coir 25 / perlite/pumice 20 / sphagnum 10 / charcoal 5

The chunky, oxygen-rich substrate for hemi-epiphytic climbing aroids.
The recipe matches the "Aroid chunky" row of Brief §4.4. The Gemini
report's *Araceae and the "Chunky Aroid Mix"* section documents the
horticultural reasoning: standard peat-perlite mixes compact and
suffocate aroid roots over time, so a bark-dominant mix with permanent
air pathways is required. The Gemini report's reference recipe is
~30% bark, ~22% coir, ~28% pumice/perlite, ~11% charcoal, ~9% worm
castings; the sprint's mandated recipe stays within that family while
substituting sphagnum for worm castings and rounding to whole-integer
percentages summing to 100.

Citations:
- `Brief §4.4` (archetype recipe)
- `Gemini-report:Araceae and the "Chunky Aroid Mix"`
- `Claude-report:Pillar A — ecological strategies` (hemi-epiphytic
  classification)

### 1.3 `succulent-gritty` — pumice/akadama 40 / coarse sand or perlite 30 / coir or pine fines 30

The fast-draining, low-fertility mix for water-storing taxa. Recipe
mirrors the "Succulent gritty" row of Brief §4.4. The "succulent /
xerophytic" ecological-strategy group in the Claude report (*Pillar A —
ecological strategies*) covers Cactaceae, Crassulaceae, *Aloe*, and the
terrestrial-rosette form of *Dracaena trifasciata* — the same group the
Gemini report (*Substrate Component Analysis*) recommends mineral-heavy
substrates for. AFP of ~25–30% is achievable in this proportions per
Gemini-report:*Substrate Physics: Porosity and Air-Water Relations*.

Citations:
- `Brief §4.4` (archetype recipe)
- `Claude-report:Pillar A — ecological strategies`
- `Gemini-report:Substrate Component Analysis and Transition to Peat-Free Media`

### 1.4 `cactus-pure-mineral` — pumice 50 / akadama/lava 30 / coarse sand 20

The all-mineral mix for the most water-sensitive cacti and mesembs
(*Lithops*, *Conophytum*) flagged in Brief §4.4. No v1 species maps to
this archetype; the entry exercises the schema so future sprints can
add *Lithops* / *Conophytum* / demanding *Cactaceae* without a JSON
migration. Justification for retaining despite zero v1 coverage is
documented in the sprint plan §4.1 ("cactus is rarely the focus in the
top-200 foliage retail set per Gemini's deep-research report").

Citations:
- `Brief §4.4` (archetype recipe)
- `docs/sprints/PLANTPOTTING-0001.md §4.1` (decision to retain unused
  archetype)

### 1.5 `epiphytic-orchid-bark` — medium fir/orchiata bark 70 / charcoal 15 / perlite 15

The bark-dominant mix for *Phalaenopsis*-class orchids with
velamen-coated aerial roots. The "Epiphytic orchid (bark)" row of Brief
§4.4 specifies 80% bark; the sprint plan's 70% bark / 15% charcoal /
15% perlite split is within the family documented in the Gemini report
*Orchidaceae and the "Epiphytic Bark Mix"* (which gives 40% bark / 30%
sphagnum / 20% perlite / 10% charcoal for *Phalaenopsis*; the sprint's
recipe is a higher-bark variant within the same archetype). Orchiata
(*Pinus radiata*) is the long-life option per
Gemini-report:*Organic Constituents* (18–24 month decomposition).

Citations:
- `Brief §4.4` (archetype recipe)
- `Gemini-report:Orchidaceae and the "Epiphytic Bark Mix"`
- `Gemini-report:Organic Constituents` (orchiata bark longevity)

### 1.6 `moisture-retentive` — coir 50 / fine/composted bark 20 / perlite 20 / long-fibre sphagnum 10

The high-water-holding mix for *Calathea/Goeppertia*, *Maranta*, ferns,
peace lily, and other terrestrial taxa that punish dry-back. Recipe
matches the "Moisture-retentive" row of Brief §4.4 (60% coir / 15%
sphagnum / 15% fine bark / minimal perlite — the sprint recipe rebalances
to 50/20/20/10 while staying inside the archetype's character).
Justified ecologically by the "terrestrial" group in the Claude report
*Pillar A — ecological strategies*, which calls out *Calathea/Goeppertia*
and *Spathiphyllum* as moisture-loving terrestrials. The Gemini report
*Substrate Component Analysis* gives sphagnum's role as "delaying
disadvantageous changes in concentration" — useful for moisture buffering.

Citations:
- `Brief §4.4` (archetype recipe)
- `Claude-report:Pillar A — ecological strategies`
- `Gemini-report:Organic Constituents` (sphagnum role)

### 1.7 `semi-hydro-inert` — LECA 100 (with hydroponic fertiliser noted in rationale)

The fully inert, passive-hydroponic substrate documented in the
"Semi-hydro inert" row of Brief §4.4 and §4.3 (the LECA bullet). No v1
species maps here either — the entry exercises the schema and supports
the future "convert to semi-hydro" recommendation lane. Because the
medium is nutritionally inert, the archetype rationale template warns
that hydroponic fertiliser is required; this is the only archetype
where omitting the fertiliser nuance would be actively harmful.

Citations:
- `Brief §4.4` (archetype recipe)
- `Brief §4.3` (LECA / semi-hydro literature)

### 1.8 `acidic-ericaceous` — acidic coir/peat-free base 45 / pine bark fines 35 / perlite 15 / acidic amendment 5

The low-pH archetype for *Saintpaulia ionantha* (African violet) and
similar calcifuges. The "Acidic / Ericaceous" row of Brief §4.4 names
this archetype. The Gemini report does not give an explicit
*Saintpaulia* recipe but Brief §4.4 documents the pH 4.5–5.5 target
and the bark-plus-acidic-base structure. The 5% "acidic amendment" line
covers ericaceous-style amendments (e.g. composted pine needles or
elemental sulphur) per Brief §4.3. The cultivar critique that prompted
adding this archetype to the v1 set is recorded in the sprint plan
§4.1 note "added per GEMINI critique for *Saintpaulia*".

Citations:
- `Brief §4.4` (archetype name + pH range)
- `Brief §4.3` (substrate-chemistry literature on acidic amendments)
- `docs/sprints/PLANTPOTTING-0001.md §4.1` (provenance of this archetype)

---

## 2. The sixteen species — per-species sourcing

The species roster is mandated by sprint plan §4.2. Below: one paragraph
per species explaining (a) why the chosen archetype suits it ecologically,
and (b) the cite handles that the species's JSON `citations` array will
carry.

### 2.1 `Monstera deliciosa` → `aroid-chunky`

Hemi-epiphytic climbing aroid; native to Central America rainforests
where it climbs trunks with thick adventitious aerial roots. Substrate
needs are documented in Gemini-report:*Araceae and the "Chunky Aroid
Mix"* — bark-dominant, fast-reaerating, low-compaction. The Claude
report *Pillar A — ecological strategies* places *Monstera* in the
"hemi-epiphytic" bucket whose substrate is bark-loose. National Garden
Bureau's "Year of the Monstera 2025" designation makes this the highest
single-species retail volume in the corpus
(Gemini-report:*Taxonomic Prevalence and Consumer Trends in 2025-2026*).
Citations: `Gemini-report:Araceae and the "Chunky Aroid Mix"`,
`Claude-report:Pillar A — ecological strategies`, `Brief §3.2`.

### 2.2 `Monstera adansonii` → `aroid-chunky`

Same hemi-epiphytic ecology as *deliciosa*; the Claude report
*Pillar A — cultivar granularity* explicitly notes "*M. adansonii* is
more epiphytic and appreciates more bark in the mix" — i.e. it is
*more*, not less, suited to the chunky archetype. Featured in
Gemini-report:*Taxonomic Prevalence* as one of the named
2025 *Monstera* hobby varieties.
Citations: `Claude-report:Pillar A — cultivar granularity`,
`Gemini-report:Taxonomic Prevalence and Consumer Trends in 2025-2026`,
`Brief §3.2`.

### 2.3 `Epipremnum aureum` (Pothos / Devil's Ivy) → `aroid-chunky`

Tropical aroid, hemi-epiphytic climber, identical substrate needs to
*Monstera* per Claude-report:*Pillar A — ecological strategies* which
groups *Epipremnum* with the climbing aroids. The "retro revival" /
"un-killable reputation" category in
Gemini-report:*Taxonomic Prevalence* notes its persistent retail volume.
Trade-name common-name confusion ("pothos" vs "devil's ivy" vs trade
"satin pothos" for *Scindapsus pictus*) is documented in
Claude-report:*Pillar A — common-name chaos*.
Citations: `Claude-report:Pillar A — ecological strategies`,
`Gemini-report:Taxonomic Prevalence and Consumer Trends in 2025-2026`,
`Brief §3.2`.

### 2.4 `Philodendron hederaceum` (Heartleaf Philodendron) → `aroid-chunky`

Hemi-epiphytic climbing aroid; chunky-aroid mix per
Claude-report:*Pillar A — ecological strategies*. The
common-name-chaos paragraph in the Claude report lists the
"heartleaf philodendron may be *P. hederaceum* or, in older
literature, *P. scandens* or *P. oxycardium* — now all synonyms" issue
this species exemplifies.
Citations: `Claude-report:Pillar A — common-name chaos`,
`Gemini-report:Araceae and the "Chunky Aroid Mix"`, `Brief §3.2`.

### 2.5 `Philodendron erubescens 'Pink Princess'` → `aroid-chunky`

Variegated cultivar. The species ecology is unchanged from other
climbing *Philodendron*; the rationale must additionally mention the
slower transpiration in chlorophyll-deficient tissue documented in
Gemini-report:*Physiological Nuances: Cultivars and Variegation
Management* ("Slower Transpiration and Risk of Saturation"). The same
report's "Light Compensation and Substrate Interaction" subsection
recommends *more* chunky material for variegated plants — a stronger
fit for `aroid-chunky` than for any moisture-leaning archetype. Brief
§1.3 H5 ("the most common failure mode is not misidentifying a species
but failing to distinguish a cultivar or variant") is exercised here.
Citations: `Gemini-report:Physiological Nuances: Cultivars and
Variegation Management`, `Brief §1.3` (H5).

### 2.6 `Spathiphyllum wallisii` (Peace Lily) → `moisture-retentive`

Terrestrial aroid native to the Tropical Americas; in the Claude report
*Pillar A — ecological strategies* it is grouped with the moisture-loving
terrestrials (the note further clarifies *Spathiphyllum* "tolerates boggy
conditions but is not truly aquatic"). The genus is in the top retail
volume per Claude-report:*Pillar A — commercial species list*.
Citations: `Claude-report:Pillar A — ecological strategies`,
`Claude-report:Pillar A — commercial species list`, `Brief §4.4`.

### 2.7 `Ficus lyrata` (Fiddle-Leaf Fig) → `standard-houseplant`

Terrestrial *Ficus*, fibrous root system; standard tropical foliage
mix. Brief §3.1 calls out the "*Ficus lyrata* and *Ficus elastica* are
in the same genus but tolerate quite different substrate moisture"
problem — *lyrata* sits at the drier end and is properly served by a
standard houseplant mix rather than a moisture-retentive one.
Gemini-report:*Taxonomic Prevalence* names *Ficus lyrata* as a leading
statement floor plant in 2025.
Citations: `Brief §3.1` (genus-level substrate divergence within
*Ficus*), `Gemini-report:Taxonomic Prevalence and Consumer Trends in
2025-2026`, `Brief §4.4`.

### 2.8 `Ficus elastica` (Rubber Plant) → `standard-houseplant`

Terrestrial *Ficus* with similar fibrous-root profile. Same standard
mix as *lyrata*; the Brief §3.1 distinction is on watering cadence
rather than archetype assignment.
Citations: `Brief §3.1`, `Claude-report:Pillar A — commercial species
list`, `Brief §4.4`.

### 2.9 `Dracaena trifasciata` (Snake Plant; **alias** `Sansevieria trifasciata`) → `succulent-gritty`

Stem-succulent ecology; in the Claude report *Pillar A — ecological
strategies* it is explicitly grouped with the "succulent / xerophytic"
taxa. The APG IV / Byng et al. (2018) transfer of *Sansevieria* into
*Dracaena* is documented in Claude-report:*Pillar A — taxonomy and
nomenclature*; the brief flags the same transfer in §3.5 ("ZZ plant
[…] will still call snake plant *Sansevieria trifasciata*"). Alias
handling is mandated by sprint plan §4.2 and §8 acceptance criteria.
Citations: `Claude-report:Pillar A — ecological strategies`,
`Claude-report:Pillar A — taxonomy and nomenclature`,
`Brief §3.5` (older sources still using *Sansevieria*).

### 2.10 `Zamioculcas zamiifolia` (ZZ Plant) → `succulent-gritty`

Tuberous, drought-adapted aroid. Despite being an aroid, its root
biology (water-storing rhizomes) places it in the succulent group per
Brief §3.2 ("water-storing tuberous roots of ZZ plant").
Gemini-report:*Taxonomic Prevalence* groups ZZ plant in the
"Low-Maintenance / Un-killable" category that drives high retail
volume.
Citations: `Brief §3.2`, `Gemini-report:Taxonomic Prevalence and
Consumer Trends in 2025-2026`, `Brief §4.4`.

### 2.11 `Chlorophytum comosum` (Spider Plant) → `standard-houseplant`

Terrestrial monocot, fibrous-root profile. The Claude report places
*Chlorophytum* in the standard "terrestrial" group; the Gemini report's
"Retro Revival" group lists *Chlorophytum comosum* as a revitalised
1970s favourite, sustaining retail volume.
Citations: `Claude-report:Pillar A — ecological strategies`,
`Gemini-report:Taxonomic Prevalence and Consumer Trends in 2025-2026`,
`Brief §4.4`.

### 2.12 `Phalaenopsis` (genus-level) → `epiphytic-orchid-bark`

Retail *Phalaenopsis* is essentially never identified to species —
the trade ships hybrid grex plants by colour. Brief §1.3 H1 / H5 and
Claude-report:*Pillar A — cultivar granularity* both note that
"*Phalaenopsis* orchids are uniformly sold as hybrids with similar
substrate needs", which is why genus-level resolution is sufficient.
The bark archetype is justified by the velamen aerial-root biology
covered in Brief §3.2 ("velamen-coated aerial roots of orchids") and
the Gemini report's recipe in *Orchidaceae and the "Epiphytic Bark
Mix"*.
Citations: `Claude-report:Pillar A — cultivar granularity`,
`Brief §3.2` (velamen biology),
`Gemini-report:Orchidaceae and the "Epiphytic Bark Mix"`.

### 2.13 `Goeppertia orbifolia` (**alias** `Calathea orbifolia`) → `moisture-retentive`

The transfer of many *Calathea* species into *Goeppertia* is part of
the same APG-IV-era reshuffling the brief warns about in §3.5. The
Claude report's *Pillar A — ecological strategies* lists "*Calathea/
Goeppertia*" together among the moisture-loving terrestrial taxa, and
Gemini-report:*Taxonomic Prevalence* lists *Calathea* 'Moonlight' under
the "Living Artwork" trend group. Alias resolution from `Calathea
orbifolia` → `Goeppertia orbifolia` is mandated by sprint plan §4.2.
Citations: `Brief §3.5`,
`Claude-report:Pillar A — ecological strategies`,
`Gemini-report:Taxonomic Prevalence and Consumer Trends in 2025-2026`.

### 2.14 `Crassula ovata` (Jade Plant) → `succulent-gritty`

Classic Crassulaceae succulent; placed in the succulent group by both
research reports (Claude *Pillar A — ecological strategies*;
Gemini-report:*Substrate Component Analysis*).
Common-name confusion ("jade plant" / "money tree" / "lucky bamboo")
is flagged in Claude-report:*Pillar A — common-name chaos* — none of
those three names point to the same plant.
Citations: `Claude-report:Pillar A — ecological strategies`,
`Claude-report:Pillar A — common-name chaos`, `Brief §4.4`.

### 2.15 `Saintpaulia ionantha` (African Violet) → `acidic-ericaceous`

The single calcifuge in the v1 set; requires pH 4.5–5.5 per Brief §4.4
("Acidic / Ericaceous") and the per-archetype note added by the sprint
plan §4.1 ("added per GEMINI critique for *Saintpaulia*"). African
violets are documented in the long-form horticultural literature
(Brief §3.3 anchor sources, RHS canon) as wanting an acidic, peat-low,
bark-fines-amended substrate.
Citations: `Brief §4.4`,
`docs/sprints/PLANTPOTTING-0001.md §4.1` (provenance),
`Brief §3.3` (RHS-canon care references).

### 2.16 `Hoya carnosa` → **blend** `{aroid-chunky 60%, succulent-gritty 40%}`

The single blend-mapped species in the v1 corpus. Hoya is documented
as "lithophytic / semi-terrestrial" in the Claude report *Pillar A —
ecological strategies* — neither purely epiphytic nor purely
xerophytic. The same report's *Pillar A — cultivar granularity*
explicitly notes the "*Hoya* terrestrial species (e.g., *H. kerrii*)
vs. epiphytic species (most others): small but real substrate
difference" tension that the blend codepath models. Brief §3.2 lists
*Hoya* under "lithophytic" examples. The 60/40 split toward
`aroid-chunky` reflects that *H. carnosa* is the predominantly
epiphytic species in the genus.
Citations: `Claude-report:Pillar A — ecological strategies`,
`Claude-report:Pillar A — cultivar granularity`,
`Brief §3.2`.

---

## 3. Alias decisions — explicit notes

### 3.1 `Sansevieria trifasciata` ↔ `Dracaena trifasciata`

Canonical name: `Dracaena trifasciata`. Retail labels and pre-2017
references overwhelmingly still use `Sansevieria trifasciata`; the
Byng et al. (2018) realignment is documented in
Claude-report:*Pillar A — taxonomy and nomenclature* and Brief §3.5.
The alias must resolve so a user typing or seeing "Sansevieria
trifasciata" reaches the correct species record. JSON: the
`Dracaena trifasciata` species record carries
`"aliases": ["Sansevieria trifasciata", ...]`; the loader's
species-index keys normalise both to the same canonical id.

### 3.2 `Calathea orbifolia` ↔ `Goeppertia orbifolia`

Canonical name: `Goeppertia orbifolia`. The transfer of many
*Calathea* species into *Goeppertia* (Borchsenius et al., 2012, and
subsequent POWO synonymy) is the same class of APG-IV-era reshuffling
flagged in Brief §3.5. Retail tags still read "Calathea". JSON: the
`Goeppertia orbifolia` species record carries
`"aliases": ["Calathea orbifolia", ...]`.

### 3.3 *Phalaenopsis* at genus level

The retail *Phalaenopsis* universe is hybrid; species-level resolution
adds zero substrate-recommendation signal and considerable
identifier-failure-mode complexity. Claude-report:*Pillar A — cultivar
granularity* ("*Phalaenopsis* orchids are uniformly sold as hybrids
with similar substrate needs") supports the decision to model
*Phalaenopsis* at the genus rank. JSON: the species `id` is
`Phalaenopsis` (no specific epithet), `scientificName` reads
`Phalaenopsis` and the rationale text names the genus rather than a
species binomial.

---

## 4. Editorial discipline

- Every JSON record must carry a non-empty `citations` array.
- Rationale text must name the species' scientific binomial (or the
  genus, for *Phalaenopsis*).
- The validator rejects `TODO`, `stub`, `lorem`, or `placeholder`
  substrings in rationale templates and species rationales.
- The blend codepath (`Hoya carnosa`) is the only entry that mixes
  archetypes; everything else maps `Single`.
- When the literature is silent and the sprint plan is silent, prefer
  doing nothing over inventing.
