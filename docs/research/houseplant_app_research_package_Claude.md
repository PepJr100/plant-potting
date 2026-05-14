# Houseplant Soil & Potting Guidance App — Full Literature Research Package

**Project:** Houseplant Identification & Substrate Recommendation Engine (Android)
**Document version:** 1.0
**Date:** 11 May 2026
**Produced by:** AI-assisted research synthesis
**Classification:** Internal — share within team and named collaborators only

---

## Contents

1. [Pillar A — Houseplant Botany, Taxonomy & Care](#pillar-a)
2. [Pillar B — Substrate Science](#pillar-b)
3. [Pillar C — Plant Identification via Computer Vision](#pillar-c)
4. [Pillar D — Android Engineering & On-Device ML](#pillar-d)
5. [Pillar E — UX, Product Design & Competitive Field](#pillar-e)
6. [Pillar F — Recommendation Logic](#pillar-f)
7. [Pillar G — Data Sources, Licensing & IP](#pillar-g)
8. [Pillar H — Privacy, Ethics & Regulation](#pillar-h)
9. [Pillar I — Market, Monetization & Growth](#pillar-i)
10. [Pillar J — Adjacent & Bonus Topics](#pillar-j)
11. [Implementation Implications Memo (IIM)](#iim)
12. [Annotated Source List](#sources)

---

<a name="pillar-a"></a>
## PILLAR A — Houseplant Botany, Taxonomy & Care

**Lead pillar:** Botany / Horticulture
**Hypothesis contact:** H1, H5

### Executive Summary

A relatively small set of plant taxa — roughly 150–200 species across 40–50 genera — accounts for the overwhelming majority of houseplants sold globally. The literature broadly supports Hypothesis 1. However, the granularity at which care recommendations must operate is finer than genus in several commercially critical lineages (notably Ficus, Monstera, Philodendron, Dracaena/Sansevieria, and orchids). Taxonomy is in active flux: the APG IV realignment and subsequent work mean that pre-2017 references routinely use names that are now synonyms or incorrect. Any database keyed to plant names must be built on Plants of the World Online (POWO) as its canonical authority and must handle synonym resolution explicitly. Pet-and-child toxicity data is available and well-curated by the ASPCA; surfacing it in-app creates both a safety benefit and a degree of legal exposure that the team must consciously manage.

### Background

The houseplant trade operates at the intersection of professional horticulture, amateur enthusiasm, and mass retail. The plant in a big-box store labelled "Tropical Foliage Mix" may be anything from *Epipremnum aureum* (a highly tolerant hemi-epiphytic aroid) to *Calathea ornata* (a demanding tropical that punishes hard water and dry air). Getting the identification right is the first step; getting the taxonomy right so that the identification maps to a care database is the second; getting the care database right is the third. All three steps require botanical grounding that is easy to underestimate when approaching the problem as a software engineering challenge.

### State of the Literature

**Taxonomy and nomenclature.** The APG IV system (Angiosperm Phylogeny Group, 2016) is the current consensus phylogeny of flowering plants. Its most commercially disruptive consequence for our product is the formal transfer of *Sansevieria* into *Dracaena* (Byng et al., 2018; confirmed in POWO). *Sansevieria trifasciata* is now *Dracaena trifasciata*; virtually every retail label still says Sansevieria, most hobbyist forums use both names, and the app must handle both without confusing users. A similar issue applies to recent Aglaonema and Scindapsus/Epipremnum boundaries in trade.

**The commercial species list.** European market data (AIPH Statistical Yearbook; Floriculture International; Zionmarketresearch 2024) consistently show that the top retail genera by volume are: *Ficus*, *Dracaena* (inc. former *Sansevieria*), *Spathiphyllum*, *Chlorophytum*, *Monstera*, *Epipremnum/Scindapsus*, *Philodendron*, *Pothos* (trade name for *Epipremnum*), *Zamioculcas*, *Syngonium*, *Calathea/Goeppertia*, *Maranta*, *Begonia*, *Crassula*, *Echeveria*, succulents generally, *Aloe*, cacti generally, *Anthurium*, *Hoya*, and several orchid genera led by *Phalaenopsis*. The total number of distinct species at meaningful retail volume is around 150–200, with a long tail of specialist/collector species. This supports H1.

**Ecological strategies and substrate relevance.** The horticultural literature (Raviv & Lieth, 2019; T1/T2) classifies houseplant roots into ecological strategies that directly determine substrate requirements:

- **Terrestrial** (fibrous roots, moderate-moisture soil): most *Ficus*, *Calathea/Goeppertia*, *Maranta*, *Chlorophytum*, *Spathiphyllum*, *Dracaena*
- **Hemi-epiphytic** (climbing with aerial roots, tolerates chunky loose substrate): *Monstera*, climbing *Philodendron*, *Epipremnum/Scindapsus*, *Syngonium*
- **Epiphytic** (velamen-coated aerial roots, bark or near-inert substrate): most orchids (*Phalaenopsis*, *Cattleya*, many *Dendrobium*), *Tillandsia*
- **Succulent/xerophytic** (thick water-storing tissues, fast-draining mineral substrate): Cactaceae, Crassulaceae, *Euphorbia*, *Aloe*, *Dracaena trifasciata*
- **Lithophytic/semi-terrestrial** (rocky crevice specialists): *Hoya* spp., some *Peperomia*, some *Begonia*
- **Aquatic/semi-aquatic**: *Spathiphyllum* tolerates boggy conditions but is not truly aquatic

The ecological strategy is the primary variable that determines which substrate archetype is appropriate and should be the first branch in the recommendation logic.

**Common-name chaos.** Trade common names are a major source of user confusion and a database design challenge. Key ambiguities:
- "Pothos" in retail = *Epipremnum aureum* (and sometimes *Scindapsus pictus*, now called "satin pothos")
- "Snake plant" = *Dracaena trifasciata* (still widely labelled *Sansevieria*)
- "ZZ plant" = *Zamioculcas zamiifolia* (stable)
- "Peace lily" = *Spathiphyllum wallisii* or any of ~50 *Spathiphyllum* spp. (substrate care is similar across them)
- "Jade plant" = *Crassula ovata*; "money tree" = *Pachira aquatica*; "lucky bamboo" = *Dracaena sanderiana* — three entirely different plants
- Many *Alocasia* cultivars are sold simply as "elephant ear," as are *Colocasia* (which are not the same genus and have different moisture needs)
- "Chinese evergreen" = *Aglaonema* spp., but dozens of cultivars with variable light requirements
- "Heartleaf philodendron" may be *Philodendron hederaceum* or, in older literature, *P. scandens* or *P. oxycardium* — now all synonyms of the same species

**Cultivar granularity and Hypothesis 5.** The literature supports H5 partially. Variegated cultivars of *Monstera deliciosa* (Thai Constellation, Albo Variegata) have substantially reduced photosynthetic capacity and thus require different light and watering cadence — though not categorically different substrate. *Phalaenopsis* orchids are uniformly sold as hybrids with similar substrate needs. The most commercially important cultivar-level substrate distinctions are:
- *Monstera deliciosa* vs. *M. adansonii*: similar substrate, but *adansonii* is more epiphytic and appreciates more bark in the mix
- Standard vs. dwarf *Crassula*: same substrate, different pot size
- *Nepenthes* highland vs. lowland: substrate nearly the same, but temperature tolerance differs
- *Hoya* terrestrial species (e.g., *H. kerrii*) vs. epiphytic species (most others): small but real substrate difference

For the recommendation engine, species-level resolution covers ~95% of use cases. Cultivar tracking is needed only for a small set of commercially important cases.

**Toxicity.** The ASPCA Toxic and Non-Toxic Plant database is the most used consumer-facing resource and is reliable for mammals. Key toxic genera in retail include: *Dieffenbachia*, *Philodendron*, *Epipremnum*, *Spathiphyllum*, *Monstera* (all contain calcium oxalate crystals; painful, rarely fatal); *Euphorbia* (irritant latex); *Nerium* (oleander; highly toxic, occasionally sold for indoor use in warm climates); *Cycas* (cycad sago palm; hepatotoxic); *Lilium* and *Hemerocallis* (extremely toxic to cats; the app should flag strongly). For most aroids the risk is oral irritation; for lilies in cat-owning households it is a genuine veterinary emergency.

### Adjudication of Working Hypotheses

**H1 (small core species set):** SUPPORTED. Approximately 150–200 species across 40–50 genera cover the vast majority of global houseplant retail. The long tail exists but is dominated by collector markets not central to a v1 product.

**H5 (cultivar misidentification is the main failure mode):** PARTIALLY SUPPORTED. Cultivar-level distinctions rarely change substrate recommendation categorically; the more dangerous failure mode for care is genus-level misidentification (e.g., confusing *Calathea* with *Aglaonema*) that sends the user to the wrong archetype entirely.

### Implications for Design and Engineering

1. The plant database must be keyed to POWO-canonical scientific names with explicit synonym fields. Every record needs: canonical name, accepted synonyms (min. 3), top 5 retail common names, and a "name confusion warning" field for high-ambiguity cases.
2. The ecological strategy classification (terrestrial, hemi-epiphytic, epiphytic, succulent, lithophytic) should be the first branching node in the substrate recommendation logic tree — not genus or family.
3. ASPCA toxicity flags should be included for every species in the database and surfaced contextually (e.g., when the user photographs a plant in a room with a cat icon on their profile). Do not bury it in fine print.
4. Nomenclature flux means the database needs a version date and a change-log. Plan for quarterly POWO checks.
5. Retire offensive common names in all user-facing copy. Use "Tradescantia zebrina" or "spiderwort" (not "Wandering Jew"); use "snake plant" (not "mother-in-law's tongue"); follow current RHS and BGCI guidance.

### Open Questions

- Which regional retail markets have substantially different species mixes (e.g., East Asia has heavier *Ficus* and lucky bamboo penetration; Scandinavia has heavier succulent penetration)? Should the app weight identification priors by user locale?
- How should the app handle the increasing volume of tissue-culture novelties — plants with retail tags that have no common name and whose scientific name is either unpublished or a trade secret?
- What is the right minimum set of images per species to robustly distinguish the top 200? This number directly constrains the training data collection plan.

### Recommended Further Reading

1. Plants of the World Online (POWO) — powo.science.kew.org — the live authority
2. Mabberley, D.J., *Mabberley's Plant-Book*, 4th ed. — single-volume taxonomy reference
3. RHS *Encyclopedia of Plants & Flowers* (Brickell, ed.) — practical care canon
4. ASPCA Toxic and Non-Toxic Plants — aspca.org/pet-care/animal-poison-control/toxic-and-non-toxic-plants
5. Aroideana (journal of the International Aroid Society) — the deepest source on aroid identification and culture

---

<a name="pillar-b"></a>
## PILLAR B — Substrate Science (Growing Media)

**Lead pillar:** Botany / Horticulture
**Hypothesis contact:** H2

### Executive Summary

The substrate science literature strongly supports Hypothesis 2: a finite set of substrate archetypes (approximately 7–9) covers the substrate needs of essentially all common houseplants. The physics of container media is well-understood and predictive; the primary variables — air-filled porosity (AFP), container capacity, and pH — can be specified as ranges for each archetype. The most significant current development is the accelerating regulatory phase-out of peat moss in the UK (retail ban in 2024) and EU (on-track for 2030), which is reshaping ingredient availability and means our recipe library should be peat-light from day one. Coco coir is the dominant replacement; pumice has emerged as the enthusiast community's preferred mineral aggregate for high-AFP mixes.

### Background

Container substrate science — distinct from field soil science — has been a coherent applied discipline since the 1960s. The seminal physical characterization framework (De Boodt & Verdonck, 1972; T1) established the measures of AFP, container capacity, and total porosity that remain standard. The textbook benchmark is Raviv & Lieth, *Soilless Culture*, 2nd ed. 2019 (T2). The discipline is applied but rigorous: industry-facing substrate standards (RHP in the Netherlands, PAS 100 in the UK, AS 3743 in Australia) operate at near-T1 rigor. For our purposes, the key insight is that the physics of a substrate in a container is counterintuitive and container-geometry-dependent — a fact that is routinely missed in consumer-facing guides.

### State of the Literature

**Physical properties.** After a container drains freely, the substrate reaches "container capacity" — a state where gravity has removed drainable water but capillary forces retain the rest. The fraction of volume that is air-filled at this point (AFP) is the single most predictive property for root-zone oxygen availability. Raviv & Lieth (2019) report that optimal AFP for most container crops is 10–30% by volume; below 10% the media becomes anaerobic under intermittent irrigation; above 35% it becomes too draining for roots to access water efficiently. For succulent and orchid substrates, higher AFP (20–40%) is intentional and appropriate.

The perched water table phenomenon (also called the "hanging water column") is the most important counterintuitive fact about containers: water retained by capillarity in the bottom zone of a container does not drain regardless of whether there are drainage holes or "crocks." Adding gravel or crocks to the bottom of a pot raises the saturated zone by exactly the depth of the gravel layer — the opposite of folk wisdom. This is documented in De Boodt & Verdonck (1972) and confirmed by multiple subsequent studies (e.g., Bilderback et al., NCSU substrate work, T3). The app should explicitly correct this misconception in its substrate guidance.

**The ingredient catalogue.** Key findings per ingredient:

- **Sphagnum peat moss:** pH 3.5–4.5, AFP 15–20%, container capacity 55–65%. Excellent wettability when moist but hydrophobic when bone dry. Subject to regulatory phase-out in UK (retail ban 2024; professional use ban from 2026 with exceptions) and progressive restriction in EU with full transition target by 2030 (Atami/Frontiers in Horticulture 2025). Environmental cost is substantial: peat extraction releases stored carbon and destroys bog ecosystems that are among the UK's most biodiverse habitats.
- **Coco coir:** pH 5.5–6.5, AFP 12–22%, container capacity 50–65%. Renewable byproduct of coconut processing. High sodium and potassium in unprocessed coir requires flushing; quality varies widely by manufacturer. Lifecycle assessments show CO2 savings of 89–109% vs peat when substituted (Treecarezone 2026 synthesis), but ocean-freight carbon cost must be included in the full LCA. Increasingly the default peat alternative in professional and consumer horticulture.
- **Perlite:** pH 7.0–7.5, AFP contribution +10–15% per 20% volume addition. Amorphous volcanic glass. Dust hazard (silica) — coarse grades should be preferred and moistened before use. Available globally in multiple grades (fine, medium, coarse, super-coarse); super-coarse ("horticultural" grade) preferred for aroid chunky mixes.
- **Pumice:** pH 6.5–7.5, AFP comparable to perlite but particles are more structurally stable under compression. The aroid enthusiast community has widely adopted pumice over perlite for chunky mixes because it does not degrade over time. Availability in the UK and Northern Europe is more limited than perlite; regional sourcing guidance is needed.
- **Vermiculite:** exfoliated mica, high CEC (~150 meq/100g), strong water retention, not recommended for succulent mixes. Historical contamination issue with Libby Mine vermiculite (asbestos) is resolved in the modern supply chain (EPA, T4) but worth noting for user communication.
- **Pine/fir bark:** particle size 6–18mm for orchid bark, 3–8mm for aroid bark mixes. Provides AFP >25% in coarse grades. Nitrogen drawdown during decomposition (microbes consume N from the fertilizer) is significant; slow-release fertilizer must be included or user instructed to fertilize supplementally. Orchiata bark (New Zealand radiata pine) is premium-grade and very consistent; availability outside Australasia and USA varies.
- **LECA (Lightweight Expanded Clay Aggregate):** near-neutral pH, AFP ~40% in container, very low CEC. The central medium for semi-hydroponic ("passive hydro") systems. The Kratky/semi-hydro literature is mostly hobbyist-origin but large and internally consistent: many aroids thrive in LECA with a nutrient solution maintained at a specific level, and the approach essentially eliminates substrate-related overwatering. Mainstream enough to warrant its own recommendation archetype.
- **Sphagnum moss (long-fibre):** distinct from peat; the gold standard substrate for *Phalaenopsis* repotted in moisture-retentive environments, for *Nepenthes* lowland species, and for prop boxes. Very high container capacity (>80%), minimal AFP when compressed — must not be packed tightly.
- **Activated charcoal:** widely recommended in terrarium and "soil-less" substrate guides for "removing toxins." The evidence base for this claim in a houseplant container context is very weak — activated charcoal loses adsorption capacity rapidly in biologically active media. Include in the database for transparency but flag the evidence gap.
- **Worm castings:** genuine evidence for seedling growth enhancement in greenhouse trials (several T1 papers). The mechanism is likely microbiological (beneficial soil microbiome transfer) rather than purely nutritional. Including 5–10% by volume in a standard houseplant mix is supportable by evidence.
- **Akadama, kiryu:** fired clay aggregates from Japan, used widely in bonsai and increasingly in succulent culture. Particle stability is moderate — akadama breaks down faster than pumice over 2–3 years. Availability outside Japan and specialist import is poor in most Western markets.

**Substrate archetypes — validated.** Synthesising the T1/T2/T3/T5 literature, the following nine archetypes cover >95% of common houseplant needs. For each, AFP (as-mixed, in a 15cm container) and pH ranges are given:

| Archetype | AFP range | pH range | Target genera |
|---|---|---|---|
| Standard houseplant | 15–25% | 5.8–6.8 | *Ficus*, *Dracaena*, *Spathiphyllum*, *Chlorophytum*, *Syngonium* (terrestrial phase) |
| Aroid chunky | 25–40% | 6.0–7.0 | *Monstera*, climbing *Philodendron*, *Epipremnum*, *Scindapsus* |
| Moisture-retentive | 8–15% | 5.5–6.5 | *Calathea/Goeppertia*, *Maranta*, ferns, *Begonia* |
| Succulent gritty | 30–45% | 6.0–7.5 | Crassulaceae, Cactaceae, *Dracaena trifasciata*, *Aloe* |
| Cactus mineral | 40–55% | 6.5–8.0 | Demanding cacti, *Lithops*, *Conophytum* |
| Epiphytic bark | 40–60% | 5.5–6.5 | *Phalaenopsis*, *Cattleya*, most *Dendrobium* |
| Semi-hydro inert (LECA) | ~40% | neutral | Aroids, *Hoya*, *Dracaena trifasciata* — passive hydro system |
| Ericaceous acid | 10–20% | 4.5–5.5 | *Camellia*, *Gardenia*, azaleas grown indoors |
| Carnivorous | 12–25% | 3.5–5.0 | *Drosera*, *Sarracenia*, *Dionaea*, *Nepenthes* |

**Peat phase-out trajectory.** The UK banned retail peat substrate sales in 2024, with professional use to follow from 2026 (with exceptions for specific crops and mushroom cultivation). The EU is targeting 2030 as the transition endpoint. Major German retailers (ALDI, REWE) committed to peat-free supply chains by 2025 (Coco Supplier 2025). The Frontiers in Horticulture review (2025, T1) documents persistent challenges: surveys across Northern Europe show user frustration with early-generation peat-free mixes, particularly around germination reliability and short-term growth performance — suggesting quality of peat-free mixes is improving but not yet parity. For the app: recommend peat-light formulations now; plan to revise all recipes to fully peat-free by 2027. Never recommend peat as the dominant ingredient in new recipes.

### Adjudication of Working Hypotheses

**H2 (finite archetype library):** STRONGLY SUPPORTED. Nine archetypes cover the overwhelming majority of houseplant substrate needs. The recommendation engine can be a lookup with a recipe library rather than a generative model.

### Implications for Design and Engineering

1. The substrate archetype is the primary output of the recommendation engine. Each archetype needs: a plain-language name, a base recipe, 2–3 regional variants (e.g., substituting pumice for perlite), a UK/EU peat-free variant, and a "what to buy at a big-box store" simplification.
2. The app should explicitly and prominently dispel the crocks-at-the-bottom myth. This is a high-impact, broadly applicable correction that users will appreciate and share.
3. AFP cannot be directly measured by a consumer but pH can (cheap test strips). Consider whether the app should prompt the user to test pH after mixing and explain why it matters.
4. Regional availability of ingredients must be flagged. A recipe using orchiata bark is not actionable in Germany without specialist ordering. Provide a tier-1 "always available" recipe and a tier-2 "optimal recipe" per archetype.
5. Peat-avoidance is not just environmental virtue — it is becoming regulatory necessity. Frame peat-free alternatives positively in copy.

### Open Questions

- For each archetype, what is the actual user failure mode? Overwatering (too high container capacity) seems far more common than underwatering; does the recipe emphasis need to reflect this?
- Can the app guide users to assess whether their potting mix has adequate drainage without a laboratory setup? Simple tests exist (press-and-check, percolation time) but the evidence base for consumer use is informal.

### Recommended Further Reading

1. Raviv, M. & Lieth, J.H. (eds), *Soilless Culture: Theory and Practice*, 2nd ed., Elsevier, 2019
2. De Boodt, M. & Verdonck, O., "Physical Properties of the Substrates in Horticulture," *Acta Horticulturae* 26, 1972
3. Bilderback, T.E. et al. — NCSU container substrate series (T3/T4)
4. Frontiers in Horticulture, "Persistent challenge of alternatives to peat," 2025
5. PRO-MIX Technical Information, Premier Tech — practical parameters for commercial mixes

---

<a name="pillar-c"></a>
## PILLAR C — Plant Identification via Computer Vision

**Lead pillar:** ML
**Hypothesis contact:** H3, H4, H5

### Executive Summary

On-device computer vision for houseplant identification is viable in 2026 at the top of the species distribution but requires careful architecture choices, domain-shifted training data, and honest confidence presentation. The PlantCLEF challenge series (now in its 2025 edition) shows that vision-transformer-based models — particularly EVA and ViT variants fine-tuned on large plant image corpora — substantially outperform CNN-based approaches on broad-spectrum plant identification benchmarks. Translated to the houseplant context, a well-tuned mid-size ViT (distilled to ~30–80M parameter range for on-device use) is expected to achieve >85% top-1 accuracy on the head 100–150 species photographed under reasonable conditions, supporting H3. However, the indoor photography distribution is substantially different from research datasets, and robust performance on the product distribution requires deliberate dataset construction targeting that domain.

### Background

Plant identification from images is a mature subfield of fine-grained visual classification (FGVC). The PlantCLEF evaluation track (part of LifeCLEF/CLEF) is the field's principal benchmark; Pl@ntNet is the field's principal deployed system. The 2024 PlantCLEF challenge introduced a new task — predicting all species in multi-label vegetation quadrat images from single-label individual-plant training data — reflecting the field's maturation toward ecological monitoring (PlantCLEF 2024 overview, arXiv 2509.15768). The 2025 edition continued this track. For houseplant identification specifically, the relevant literature skews toward the individual-plant classification problem, which is well-represented by earlier PlantCLEF editions and by the PlantNet-300K dataset.

### State of the Literature

**Architecture landscape.** PlantCLEF 2023 results (arXiv 2509.17622) established clearly that ViT-based approaches outperform CNN-based ones at scale: the best CNN run achieved MA-MRR of 0.618 vs 0.674 for the leading EVA ViT approach. This delta is consistent with the broader FGVC literature. For on-device deployment, however, full ViT models are too large. The relevant architectures for our product are:

- **MobileViT** (Mehta & Rastegari, 2021): merges CNN and ViT blocks; 5–7M parameters; competitive accuracy on ImageNet at 256px
- **FastViT** (Vasu et al., Apple, 2023): uses structural reparameterisation; superior accuracy/latency tradeoff on mobile benchmarks; available as TFLite-compatible weights
- **MobileNetV4** (Google, 2024): introduces Universal Inverted Bottleneck; strong on mid-range SoCs
- **EfficientNet-Lite variants**: widely deployed in mobile plant apps; 4–13M parameters; well-supported by TFLite/LiteRT
- **Distilled ViT (DeiT-Tiny/Small)**: ViT distilled from a larger teacher model; Small variant (~22M params) achieves near-full-size ViT accuracy at substantially lower compute

For houseplant classification specifically — where the number of classes is small (150–200 species) and the training data can be dense within that scope — a distilled model fine-tuned on a houseplant-specific dataset will likely achieve better accuracy than a general plant model applied to houseplant photos. The domain gap between PlantNet-300K (field flora, natural lighting, botanical angles) and indoor plant photography (artificial lighting, decorative pots, partial views) is significant.

**Dataset landscape.** No published dataset is optimised for indoor houseplant identification under realistic consumer conditions. Available datasets and their fitness for purpose:

- **PlantNet-300K** (Garcin et al., NeurIPS 2021): 306k images, 1081 species, CC-BY licensed for research. Good starting point; species selection skews European field flora and most species are photographed outdoors. Usable for pretraining.
- **Pl@ntNet observation corpus**: tens of millions of images, many species, mixed CC licensing per observer. The houseplant species within this corpus can be extracted. High quality for the head of the distribution.
- **iNaturalist plant subset**: millions of research-grade and casual-grade observations. Strong on temperate flora. Useful but outdoor-dominated.
- **GBIF media corpus**: enormous, indexed by species, heterogeneous quality. Useful for supplementing rare species.
- **Hobbyist community images** (Reddit, Instagram, Pinterest): the closest proxy to the deployment distribution — indoor lighting, decorative settings, partial views. Licensing is problematic; these cannot be scraped at scale without legal risk, but can inform image collection protocols and augmentation strategies.
- **Purpose-collected dataset (recommended)**: 5,000–20,000 images per top-50 species, photographed under realistic indoor conditions by a crowdsourcing operation or nursery partnership. This is the highest-leverage investment in model quality.

**Domain shift and robustness (H4).** The literature on domain shift in plant ID (notably Bonnet, Joly et al. in the Pl@ntNet active learning papers) consistently shows that models trained on curated research images degrade on user-submitted images. The degradation factors are: background clutter, partial occlusion (only one leaf visible), poor lighting (under-exposure, cool white LED), juvenile specimens, stressed/discoloured specimens, and decorative pots that confound the model. Standard augmentation (colour jitter, random crop, random flip) partially addresses this; test-time augmentation improves confidence calibration. For H4: the literature supports the premise. Users will photograph plants in conditions substantially worse than training datasets. This must be addressed by: (a) training on a dataset that includes such conditions, (b) training an uncertainty estimator or using temperature scaling for calibration, and (c) designing the UX to accept multi-shot inputs (whole plant + leaf close-up + new growth).

**Confidence calibration.** Guo et al. (ICML 2017) showed that modern neural networks are systematically overconfident — their reported softmax probability is higher than their empirical accuracy. Expected Calibration Error (ECE) is the standard metric. Post-hoc calibration methods (temperature scaling, Platt scaling) can reduce ECE substantially at inference time with no change to model architecture. For a consumer-facing product that shows users a confidence percentage, ECE must be <0.05 to avoid systematically misleading users. The recent fine-grained calibration literature (several CVPR papers 2022–2024) shows that ECE is higher for visually similar classes — exactly the condition we care about (e.g., *Monstera deliciosa* vs *M. adansonii*). Temperature scaling applied per-class is recommended.

**Open-set recognition.** Users will photograph non-plant objects, pets, walls, and non-houseplant plants. The model must handle out-of-distribution inputs gracefully. Geng et al. (IEEE TPAMI 2021) survey open-set recognition methods. For our case: training a threshold on the maximum softmax probability works at moderate scale; energy-based out-of-distribution detection (Liu et al., NeurIPS 2020) is more robust. A simple implementation: if max softmax < 0.4 after calibration, the app returns "I'm not sure this is a recognisable houseplant — try photographing a single leaf or the whole plant more clearly."

**Multimodal approaches.** Frontiers in Plant Science (2025) published results from fused multimodal deep learning on the PlantCLEF dataset, achieving 82.61% accuracy on 979 classes by combining image and text modalities. CLIP-style models (Radford et al., ICML 2021; SigLIP 2023) trained on large web corpora have implicit botanical knowledge and have shown strong zero-shot plant classification capability. For v1, a vision-only approach fine-tuned on a houseplant dataset is likely sufficient and simpler; multimodal approaches are a compelling v2 research direction, particularly for rare species disambiguation where textual context ("grows as an epiphyte," "has velvet-textured leaves") can disambiguate cases where images cannot.

### Adjudication of Working Hypotheses

**H3 (on-device CV is sufficient):** CONDITIONALLY SUPPORTED. On-device inference with a FastViT or MobileViT architecture (30–80MB model) can achieve >85% top-1 on the head 100 species under good conditions. For the tail (species 100–200) and under degraded conditions (poor lighting, partial views), accuracy drops significantly and the app should fall back to multi-shot or low-confidence hedging rather than a confident wrong answer. A cloud fallback for genuinely ambiguous queries is prudent.

**H4 (deployment distribution is much worse than research datasets):** STRONGLY SUPPORTED. Indoor lighting, decorative backgrounds, and partial plant views are ubiquitous in user photography and significantly degrade models trained on botanical datasets.

**H5 (cultivar confusion is the main failure mode):** PARTIALLY SUPPORTED. The model can distinguish most genus-level pairs confidently; species-within-genus is harder (e.g., *Monstera deliciosa* vs *M. adansonii*); cultivar-within-species (standard vs variegated *Monstera*) is very hard from photos alone and is not a critical care-recommendation failure mode.

### Implications for Design and Engineering

1. Start from a ViT or MobileViT backbone pre-trained on PlantNet-300K or the Pl@ntNet observation corpus, then fine-tune on a purpose-collected 150-species indoor houseplant dataset.
2. Apply temperature scaling as a post-hoc calibration step before deployment. Target ECE < 0.05 on a held-out indoor test set.
3. Implement energy-based or max-softmax open-set detection; the UX must handle "I don't recognise this" gracefully.
4. The multi-shot interface (whole plant + leaf close-up + stem/petiole) is strongly motivated by the literature and by the community understanding that certain disambiguations (e.g., *Monstera* species, *Philodendron* species) require petiole or new-leaf morphology.
5. Evaluate ECE and top-1 accuracy separately for the "confident" region (max softmax > 0.7) and the "uncertain" region. The confident region should drive product decisions; the uncertain region drives UX refinement.

### Open Questions

- Is a cloud inference fallback for the bottom 20% of the confidence distribution worth the privacy, latency, and cost tradeoffs?
- What is the minimum labeled training sample count per species to achieve stable top-1 accuracy? The literature suggests ~200–500 well-curated images per class at MobileViT scale, but this needs empirical validation on the houseplant distribution.
- Can a CLIP-based zero-shot baseline handle rare species well enough to delay the need for labeled data in the long tail?

### Recommended Further Reading

1. Garcin et al., "Pl@ntNet-300K," NeurIPS Datasets & Benchmarks 2021
2. PlantCLEF 2023 overview, arXiv 2509.17622 — architecture comparison
3. Guo et al., "On Calibration of Modern Neural Networks," ICML 2017
4. Geng et al., "Recent Advances in Open Set Recognition," IEEE TPAMI 2021
5. Vasu et al., "FastViT," Apple 2023 — best current mobile ViT architecture

---

<a name="pillar-d"></a>
## PILLAR D — Android Engineering & On-Device ML

**Lead pillar:** Android
**Hypothesis contact:** H3

### Executive Summary

The Android engineering landscape in 2026 is well-settled around Kotlin/Coroutines, Jetpack Compose, and a Hilt-wired architecture. The ML inference stack has consolidated around LiteRT (formerly TensorFlow Lite) and ONNX Runtime Mobile, with hardware acceleration available on most mid-range-and-above SoCs via GPU delegates and, on flagship devices, dedicated NPU paths. The most significant 2025 shift is the deprecation of NNAPI as of Android 15 (API 35) in favour of the ML Core API, which requires teams to use LiteRT's delegate abstractions rather than calling NNAPI directly. Android 14+ also introduced a new partial photo access model that significantly changes how the camera/gallery permissions flow must be designed.

### Background

Android development has consolidated firmly by 2026. The "modern Android development" (MAD) stack — Kotlin, Coroutines, Jetpack Compose, Navigation, Room, DataStore, WorkManager, Hilt — is the unambiguous platform direction. The Now in Android reference architecture (github.com/android/nowinandroid) is the canonical implementation reference. For on-device ML, Google has rebranded TFLite to LiteRT (2024) and is unifying its inference runtime strategy. MediaPipe continues to provide high-level task APIs that wrap LiteRT. Play Store policies for AI features have added requirements for AI-generated content disclosure that apply to apps whose AI outputs are surfaced to users.

### State of the Literature

**Architecture.** The MVVM + Unidirectional Data Flow (UDF) pattern remains dominant, implemented via ViewModels exposing StateFlow/SharedFlow to Compose-based UI. The brief's working hypothesis about Compose performance concerns is largely resolved: Baseline Profiles (introduced in Android 12, matured in 14/15) eliminate most first-run JIT latency; the Compose compiler has improved phase semantics to reduce unnecessary recompositions. The recommended architecture for this product: single-activity with Navigation Compose; feature modules for identification, recommendation, plant library, and settings; a shared data module with Room + DataStore; an inference module encapsulating the ML pipeline.

**CameraX and image preprocessing.** CameraX is the correct library for this app. The CameraX image analysis use case provides frames as `ImageProxy` in `YUV_420_888` format; conversion to RGBA bitmap (required by most TFLite models) must be done efficiently using RenderScript (deprecated but still available) or the newer Pixel Copy API. The most reliable 2025 pattern uses a pixel buffer pool to avoid repeated allocation. For a plant ID app, the image capture quality (focus, exposure, framing guidance) matters significantly for model performance; consider implementing a live viewfinder guide with feedback ("move closer," "ensure leaf is in frame") using CameraX image analysis before triggering capture.

**LiteRT / TFLite inference stack.** As of Android 15 (API 35), NNAPI is formally deprecated; Google recommends migrating to the LiteRT GPU delegate for GPU acceleration and the new ML Core API for NPU access. Practical impact: use `Interpreter(model, options)` with `GpuDelegate` added via `addDelegate()`; fall back to CPU on devices without GPU delegate support. The GPU delegate achieves 2–5x speedup over CPU on a MobileViT-S class model on a Snapdragon 720G class SoC (typical mid-range). Target inference latency: <500ms on a Snapdragon 720G or MediaTek Dimensity 700 equivalent (the mid-range Android floor as of 2025). A MobileViT-S model at INT8 quantization fits in ~20–30MB and achieves this target.

**ONNX Runtime Mobile** is a viable alternative to LiteRT and is particularly relevant if the team decides to use a PyTorch-trained model: export to ONNX, then use ONNX Runtime Mobile on-device. The delegate ecosystem is slightly less mature than LiteRT's for Android but is improving.

**Model packaging.** For a model in the 20–80MB range: Play Asset Delivery (PAD) with install-time delivery is the recommended approach. The model ships as a separate asset pack that the Play Store delivers alongside the APK; users never experience a "downloading model" step on first launch. If the model is >80MB or if a cloud-first strategy is chosen, on-demand PAD delivery works but requires designing a "downloading model..." first-launch flow. Shipping the model inside the APK is appropriate only if it is <30MB after compression (APK size limits and download experience).

**Android 14 photo permission changes.** Android 14 introduced a redesigned permission model for photo/media access. The new `READ_MEDIA_VISUAL_USER_SELECTED` permission enables partial photo access (user can select specific photos to share with the app). For a plant ID app, the primary intent is camera capture (no permission needed beyond `CAMERA`), with optional gallery selection for identification of an existing photo. The recommendation: default to in-app camera, make gallery selection an explicit secondary path, and request `READ_MEDIA_VISUAL_USER_SELECTED` only when the user explicitly taps "Choose from gallery." This minimises permission footprint and avoids the anxiety that broad gallery permission requests create.

**Privacy — Data Safety form.** The Google Play Data Safety form requires explicit declaration of: photos/videos collected (if the app uploads any image to a server), approximate location (if used), personal data (if user accounts are created). If all inference is on-device and no image is uploaded, the data collection footprint is minimal. If cloud fallback inference is implemented, the relevant data type is "Photos and videos — Not shared with third parties — Not encrypted in transit" (unless you use HTTPS, in which case "encrypted in transit"). Pre-completing the Data Safety form before submission saves several days of back-and-forth with the reviewer.

**Performance.** Key targets: cold start <2s (feasible with Baseline Profiles and deferred model loading); inference <500ms for top-80% of devices; no jank (dropped frames) during viewfinder operation (use a dedicated thread pool for image analysis callbacks, separate from the main thread). Firebase Performance Monitoring is the lowest-friction observability tool; Macrobenchmark is the correct local profiling tool for startup and rendering metrics.

**Accessibility.** Compose's semantic API (semantics modifier, `contentDescription`) is the access point for TalkBack. Every button, every plant card, and every confidence display must have a meaningful content description. The camera viewfinder presents a specific challenge for blind or low-vision users — consider a haptic feedback approach when the model detects a plant-shaped object in frame.

### Adjudication of Working Hypotheses

**H3 (on-device is sufficient):** On a Snapdragon 720G or better — which covers the majority of active Android devices as of 2025 — a 20–30MB INT8-quantized MobileViT-S model achieves sub-500ms inference with the GPU delegate. Below this SoC tier (entry-level devices, ~$100 price point), CPU-only inference at ~1–2s is acceptable if the UX frames it correctly. Cloud fallback is recommended for <2% of devices and for genuinely ambiguous queries, not as the default path.

### Implications for Design and Engineering

1. Use Play Asset Delivery (install-time) for model delivery. This eliminates first-run model-download friction and keeps the APK small.
2. Default to camera-only flow; request `READ_MEDIA_VISUAL_USER_SELECTED` only on user-initiated gallery selection.
3. Apply the LiteRT GPU delegate by default with CPU fallback. Avoid calling NNAPI directly on API 35+.
4. Implement Baseline Profiles before beta launch; cold-start above 2s will drive Day-1 churn.
5. The Data Safety form should be completed and internally reviewed before first submission. If cloud inference is added, re-review the "photos and videos" declaration.

### Open Questions

- What is the correct minimum-API target? API 26 (Android 8.0) covers ~96% of active devices but some Jetpack Compose features require API 21 minimum. API 24 is the pragmatic minimum for performance-sensitive apps.
- Does the app need a user account, or can plant records be stored locally (Room) with optional cloud sync? Offline-first + optional sync dramatically simplifies the privacy posture.

### Recommended Further Reading

1. Android Developers docs — developer.android.com/architecture
2. Now in Android (github.com/android/nowinandroid) — canonical reference codebase
3. LiteRT documentation — ai.google.dev/edge/litert
4. CameraX documentation — developer.android.com/training/camerax
5. Moskała, M., *Kotlin Coroutines: Deep Dive*, Kt. Academy Press

---

<a name="pillar-e"></a>
## PILLAR E — UX, Product Design & Competitive Field

**Lead pillar:** UX/Product
**Hypothesis contact:** H3, H6

### Executive Summary

The plant identification app category is large, growing, and quality-uneven. The dominant player — PictureThis — generates ~$5M/month in revenue (Sensor Tower, April 2026) but is widely criticised for aggressive subscription paywalls and opaque accuracy claims. The most trusted product in the category — Pl@ntNet — is free, research-backed, and better on wild flora than indoor plants. The gap our product occupies is: houseplant-specific, substrate-advice-first, honest about confidence, and monetised fairly. The UX literature on confidence presentation, multi-shot camera capture, and care-recommendation display is small but actionable. Material Design 3 provides an adequate design foundation; the differentiation must come from content quality and honest AI presentation.

### Background

The plant ID app category grew dramatically during the 2020–2021 pandemic period and has stabilised at structurally elevated levels. By 2024, the global plant identification apps market was valued at approximately $175–210 million (Verified Market Research; DataIntelo), growing at ~14% CAGR. The category is dominated by freemium subscription models (76.88% market share by revenue) with aggressive paywalls. User review sentiment across PictureThis, Blossom, and PlantIn consistently identifies three pain points: (1) being hit with a paywall before getting any useful result, (2) confident-sounding identifications that are wrong, and (3) generic care advice that ignores their specific plant's needs.

### State of the Literature

**Competitive audit.** Based on available public data (Sensor Tower, user reviews, app store listings) and category analysis as of May 2026:

- **PictureThis (Glority):** Market leader by installs and revenue (~$5M/month on iOS App Store alone). Identification accuracy is claimed but not documented. Heavy paywall — identification results are blurred until subscription. Aggressively monetised with annual subscription ($29.99/year). High App Store rating maintained by large review volume; negative reviews concentrate on accuracy and paywall behaviour. This is the incumbent to differentiate against, not to copy.
- **Pl@ntNet:** Free, research-backed (CNRS/INRAE). Best accuracy on European wild flora; weaker on retail houseplants. No paywall. Care advice is minimal. Honest confidence presentation (shows confidence score and alternative candidates). The honesty model to emulate.
- **Planta:** Swedish design aesthetic; strong care-reminder functionality; identification is secondary. Subscription-gated care features. Well-regarded for UX quality. Not substrate-advice focused.
- **Greg:** Community-flavoured; watering reminder focused; social features. Not substrate advice focused.
- **Seek (iNaturalist):** No paywall; honest about uncertainty; shows taxonomy-grade confidence. Excellent model for confidence UX. Weak on care advice.
- **Google Lens:** The zero-paywall competitor. Identifies most common houseplants accurately for free. Cannot offer substrate-specific advice. The floor against which all paid products must justify their value.

**Confidence presentation.** The HCI literature (HAX Toolkit, Microsoft Research; PAIR Guidebook, Google) converges on several principles for AI uncertainty display: (1) show a small number of alternatives rather than a single binary answer; (2) avoid numerical probabilities for lay users unless calibrated and explained; (3) use a ranked-list with explanation of the distinguishing features between top candidates; (4) provide a clear pathway for the user to correct the model. For a houseplant app: "Most likely: *Monstera deliciosa* — distinguishing feature: fenestrated leaves. Alternative: *Monstera adansonii* — smaller leaves with full holes rather than splits. Tap to select the correct plant." This pattern is better than "94% confident."

**Camera flow.** The multi-shot approach is well-motivated by both the plant biology literature (Pillar A) and the CV literature (Pillar C). Recommended flow: (1) whole-plant shot required; (2) leaf close-up optional but guided; (3) new growth/petiole optional. Pl@ntNet pioneered the organ-type multi-shot UI; it is effective and should be adopted and adapted. The framing guidance (live overlay with "centre the plant," "get closer to the leaf") improves image quality at the input stage and is worth implementing for the identification quality improvement alone.

**Recommendation presentation.** The substrate recommendation is the product's primary differentiation. The UX must balance completeness (the user who wants the full recipe) with actionability (the user who just wants to know what to buy). Recommended pattern: progressive disclosure. Layer 1: "This plant needs a chunky, airy mix. It hates sitting in wet soil." Layer 2: "Recipe: 40% orchid bark, 30% coco coir, 20% perlite, 10% worm castings." Layer 3: "Why? *Monstera* is a hemi-epiphyte in nature — its roots cling to tree bark in humid forests. Dense soil suffocates them." Layer 4: "Can't find orchid bark? Try 50% perlite + 40% coco coir + 10% worm castings." This four-layer pattern covers beginners through enthusiasts.

**Monetisation.** The community consensus (r/houseplants, App Store reviews) is strongly negative about: paywalls before identification results; annual subscription presented as the only option; "unlimited identifications" as the only paid value. Positive monetisation patterns that have been successful: one-time purchase unlocking the full knowledge base; subscription unlocking plant library/journal features (not identification itself); premium archetype deep-dives. Our recommendation: free identification with a daily soft limit (5 per day free, unlimited paid); substrate recipe always free; full recipe notes + sourcing guide + repotting calendar gated.

**Naming sensitivity.** The horticultural trade has been moving away from offensive common names; the app should lead, not lag. Adopt "Tradescantia zebrina" or "spiderwort"; "snake plant" or "Dracaena trifasciata"; "mother-of-thousands" for *Kalanchoe daigremontiana*. The RHS Plant Finder has been implementing similar guidance.

### Adjudication of Working Hypotheses

**H6 (liability and ethical risk):** SUPPORTED but manageable. The competitive field shows no evidence of significant legal action against plant ID apps to date, but the pattern of confident-wrong identifications (particularly toxic-vs-nontoxic confusions) creates real risk. The mitigation is: display confidence honestly, surface toxicity warnings proactively, and include a clear disclaimer that recommendations are advisory. This is UX design, not just legal boilerplate.

### Implications for Design and Engineering

1. Never blur identification results behind a paywall. The paywall friction should come after the value is delivered.
2. Show top-2 identification candidates with distinguishing visual features, not a single confident answer.
3. Implement multi-shot organ-type capture (whole plant + leaf + petiole); this is both better UX and better model performance.
4. Use progressive disclosure for the substrate recommendation: one-sentence summary → recipe → explanation → substitutions.
5. Include a toxicity warning in-line for any plant confirmed as toxic to cats, dogs, or children; make it impossible to miss.

### Open Questions

- What is the right daily free identification limit to drive conversion without driving churn? A/B testing is required; the literature does not answer this.
- Is a social/community feature (share your plant, ask the community) worth the moderation cost? The community is the long-term moat but the moderation burden is real.

### Recommended Further Reading

1. Google PAIR Guidebook — pair.withgoogle.com/guidebook
2. Microsoft HAX Toolkit — microsoft.com/en-us/haxtoolkit
3. Material Design 3 documentation — m3.material.io
4. Nielsen Norman Group — mobile UX and AI uncertainty display articles
5. Sensor Tower and data.ai category intelligence (subscription required)

---

<a name="pillar-f"></a>
## PILLAR F — Recommendation Logic

**Lead pillar:** UX / Product (user-facing), Botany (content)
**Hypothesis contact:** H2, H5

### Executive Summary

A rule-based recommendation engine with a well-curated knowledge base is the correct v1 architecture. The case for a learned recommender is weak until significant outcome data is available. The knowledge base should be structured as: species → ecological strategy → substrate archetype → recipe → regional variants, with explicit handling of uncertain identification inputs (confidence < threshold → recommend the archetype shared by all plausible candidates, or ask the user to help disambiguate). The recommender-systems literature (Ricci et al., 2022) is primarily useful for the long-term roadmap; the more immediately relevant literature is on explainable AI and human-AI interaction design (PAIR, HAX Toolkit).

### Background

The recommendation problem for substrate advice is unlike typical collaborative-filtering recommendation because there is no user-to-user preference signal — the correct substrate for a *Monstera* does not depend on what other *Monstera* owners liked; it depends on the plant's biology. The relevant literature therefore comes from three sources: horticultural science (what does this plant actually need), knowledge representation (how to structure the database), and human-AI interaction (how to present the recommendation such that the user trusts and acts on it).

### State of the Literature

**Rule-based vs. learned.** For domains with small cardinality, ground truth in expert knowledge, and limited outcome data, rule-based systems are well-established as the correct architecture (Aggarwal, 2016; Ricci et al., 2022 both note this directly). The substrate recommendation domain has all three properties. The risk with a rule-based system is brittleness at the edges (what do we recommend for a plant we haven't seen?) — which argues for maintaining the rule base actively and having a fallback to the most common archetype with explicit uncertainty.

**Knowledge base structure.** The literature on ontology-light knowledge representation (SKOS, lightweight typed records) argues for a flat relational structure over a full OWL ontology for this application: a `plants` table (species → common names → ecological strategy → archetype_id → toxicity), an `archetypes` table (archetype_id → name → base recipe → AFP range → pH range), a `recipes` table (ingredient → fraction → substitutes), and a `regional_availability` table (ingredient → available_in_regions). This structure is easily queried, easily updated by a single curator, and easily versioned. SQLite/Room is adequate for v1.

**Confidence propagation.** When identification confidence is split between two species with different substrate needs (e.g., *Monstera deliciosa* vs. *Calathea ornata* — one chunky aroid, one moisture-retentive tropical), the recommendation must hedge. The correct behaviour is: if the two candidates share an archetype, proceed confidently; if they do not, ask the user one disambiguation question ("Does this plant have large, fenestrated leaves, or smaller, striped leaves?"). Never output a confident wrong recommendation; the literature on trust in human-AI systems (PAIR Guidebook; Microsoft HAX Toolkit CHI 2019) consistently shows that confident wrong answers cause greater trust damage than acknowledged uncertainty.

**Explainability.** The one-sentence "why" is critical for user trust. The evidence from advisory AI systems (both in financial and health domains) shows that unexplained recommendations are followed less reliably than explained ones, and that over-complex explanations are as bad as none. The sweet spot is one to two sentences connecting the plant's biology to the substrate property: "Phalaenopsis orchid roots need air around them to stay healthy — dense soil traps moisture and causes root rot, so we recommend a bark-only mix that dries out quickly between waterings."

**Personalisation signals.** The literature on personalisation in advisory apps (Ricci et al., 2022; several user research papers in CHI) suggests that the highest-value personalisation signals for substrate advice are: (1) water hardness (affects whether to recommend distilled water or tap water for acidic-substrate plants); (2) climate/humidity zone (affects how quickly substrates dry out); (3) pot type (terra cotta vs. plastic/ceramic — affects watering frequency more than substrate). These are worth collecting during onboarding with explicit explanation of why they matter.

**Evaluation.** In the absence of long-term outcome data ("my plant died / thrived"), the evaluation protocol for the recommendation engine must rely on: (a) expert horticultural review of a stratified sample of recommendations; (b) consistency tests (same plant photographed twice should get the same recommendation); (c) adversarial probes (plants deliberately photographed in bad conditions should either get the right recommendation or explicitly hedge). A panel of 3–5 horticultural experts reviewing 50 sampled recommendations monthly is a tractable quality assurance mechanism.

### Adjudication of Working Hypotheses

**H2 (finite archetype library):** SUPPORTED from the recommendation angle. The rule-based engine with 9 archetypes covers the product scope; the recommendation engine need not be generative.

### Implications for Design and Engineering

1. Ship a rule-based engine with a curated knowledge base. Design the schema for extensibility (adding new species, new archetypes, regional variants) without rewriting the engine.
2. Implement explicit confidence routing: high confidence → direct recommendation; split confidence between compatible archetypes → direct recommendation with note; split confidence between incompatible archetypes → disambiguation question.
3. Every recommendation must display a one-to-two sentence "why" that connects biology to substrate.
4. Include a "user feedback" signal from day one: "Did this recommendation work for you?" (👍/👎). This seeds the outcome dataset for a future learned layer.
5. Regional availability is first-class: every recipe should have a "simplified version using widely available products" fallback.

### Open Questions

- How do we handle the increasing market of pre-mixed commercial substrates (e.g., "Miracle-Gro Tropical Mix")? Should we rate or compare commercial mixes, or only output DIY recipes?
- At what scale of outcome data does a learned recommendation layer become worth training? Rough estimate: 10,000+ user feedback signals with reasonable signal-to-noise.

### Recommended Further Reading

1. Ricci, F., Rokach, L. & Shapira, B. (eds), *Recommender Systems Handbook*, 3rd ed., Springer, 2022
2. Google PAIR Guidebook — pair.withgoogle.com/guidebook
3. Microsoft HAX Toolkit (CHI 2019) — human-AI interaction patterns
4. Aggarwal, C.C., *Recommender Systems: The Textbook*, Springer, 2016
5. Pillar B substrate archetype literature (above)

---

<a name="pillar-g"></a>
## PILLAR G — Data Sources, Licensing & Intellectual Property

**Lead pillar:** Legal / Policy
**Hypothesis contact:** N/A (enabling pillar)

### Executive Summary

The data licensing landscape for this product is manageable but requires careful per-source analysis. The major scientific data providers — Pl@ntNet, GBIF, iNaturalist — each have distinct licenses and requirements. None permits unrestricted commercial use of images without attribution at minimum, and several require share-alike provisions that would constrain a commercial app's database. Model weights licensing for the planned architectures (MobileViT, FastViT, EfficientNet-Lite) is generally permissive. The largest unresolved risk is user-generated content: our own ToS must explicitly license user-uploaded photos for model training, clearly disclosed and fairly presented.

### Background

Data licensing in ML has become a materially important issue since approximately 2022; several large-scale web-scraping legal cases (hiQ v. LinkedIn; Getty Images v. Stability AI; multiple newspaper suits) have made it untenable to assume that "publicly accessible = legally usable for commercial ML." For a plant identification app, the risk is lower than for a general-purpose image model because the relevant datasets were collected by citizen scientists with conservation intent — but the specific license terms still matter and vary.

### State of the Literature

**Creative Commons.** Most citizen-science plant image databases use CC licensing. Key variants:
- **CC0 / Public Domain Mark:** No restrictions. Ideal. Rare in large databases.
- **CC BY:** Attribution required. Commercially usable. GBIF's preferred license; many Pl@ntNet observations.
- **CC BY-SA:** Attribution + share-alike. Fine for open releases; problematic if we want to keep our training data set proprietary. The share-alike obligation applies to derivative databases, not necessarily to trained model weights (this is legally unsettled in EU).
- **CC BY-NC:** Non-commercial. Cannot be used for a commercial app or ad-supported product. iNaturalist observers can choose this; records must be screened and excluded.
- **CC BY-ND:** No derivatives. Unusable for training.

**GBIF.** The Global Biodiversity Information Facility (gbif.org) requires a citation following its standard format and asks that datasets be cited even when accessed via API. Commercial use is permitted for CC BY and CC0 records. The share-alike concern applies to the underlying datasets (many contributed datasets have BY-SA licenses). GBIF's terms permit downloading data for computational analysis; creating derived training datasets is a grey area for BY-SA records that the team should formally seek legal opinion on.

**Pl@ntNet API.** As of 2026, the Pl@ntNet API terms permit use for non-commercial research and explicitly require contacting the Pl@ntNet team for commercial licensing arrangements. Using Pl@ntNet as a data source for a commercial product requires a licensing agreement with INRAE/CNRS. Budget for this negotiation; it is achievable but takes 3–6 months and may involve a revenue share or per-API-call fee.

**iNaturalist.** Observations carry the license chosen by the observer (CC0, CC BY, CC BY-NC, etc.). The iNaturalist API allows querying by license type; filtering to CC0 + CC BY records produces a usable subset. Importantly, iNaturalist's terms explicitly prohibit scraping and require API use with attribution. iNaturalist research-grade records filtered to CC BY-compatible are a valuable training source for realistic field conditions.

**POWO / Kew.** Plants of the World Online (POWO) botanical data is freely accessible for lookup purposes and citations are requested rather than legally required. Scraping POWO at scale to build a database is not explicitly prohibited but is outside the intended use; licensing discussion with Kew is recommended for a commercial product.

**User-uploaded images.** The app's own Terms of Service must include a clause granting the operator a non-exclusive, royalty-free, perpetual licence to use uploaded images for improving the identification model, with clear disclosure during onboarding. This clause must be GDPR-compliant: users must be able to withdraw consent, which means images must be individually attributable to users in the training dataset (soft-link, not embedded) to support deletion requests.

**Model weights licensing.** Key architectures:
- **MobileViT (Apple):** MIT license — fully permissive commercial use
- **FastViT (Apple):** Apache 2.0 — fully permissive commercial use
- **MobileNetV4 (Google):** Apache 2.0
- **EfficientNet-Lite (Google):** Apache 2.0
- **PlantNet-300K dataset:** CC BY-SA 4.0 — share-alike applies to the dataset; uncertain application to fine-tuned model weights. Legal opinion recommended before public release.
- **CLIP (OpenAI):** MIT license for the original; many derived models are Apache 2.0

**EU database rights.** The sui generis database right (Directive 96/9/EC) protects substantial investment in collecting, verifying, or presenting database contents. GBIF and iNaturalist databases may attract this protection in the EU, meaning extraction of a substantial part requires a license even for data that is CC-licensed at the record level. UK retained equivalent database rights post-Brexit. For programmatic API access with attribution, the practical risk is low; for bulk extraction of large dataset fractions, formal licensing is prudent.

### Implications for Design and Engineering

1. Build the training dataset using only CC0, CC BY, and CC BY-SA records — document every source in a data provenance log.
2. Seek a commercial licensing discussion with Pl@ntNet/INRAE before v1 launch if the app will use Pl@ntNet as a backend fallback; do not ship with undisclosed API calls.
3. Implement a data-subject deletion pipeline for user-uploaded images before any model training begins on UGC.
4. The ToS "model improvement" clause must be drafted by a lawyer, displayed prominently during onboarding (not buried in paragraph 18), and accompanied by a clear opt-out mechanism.
5. All model weights used must be verified as commercially permissive; document license compliance in the project's legal file.

### Open Questions

- Does training on CC BY-SA data produce model weights that are themselves SA-licensed? This is genuinely unsettled in EU law; a formal legal opinion is needed before public release.
- Can we partner with nurseries or botanical gardens for purpose-collected CC0 image datasets that avoid these constraints entirely?

### Recommended Further Reading

1. Creative Commons license documentation — creativecommons.org/licenses
2. GBIF Data Use Agreement and Citation Guidelines — gbif.org/citation-guidelines
3. iNaturalist API Documentation and Terms of Service
4. Data Provenance Initiative (dataprovenance.org) — ML dataset licensing analysis
5. EU Database Directive 96/9/EC and UK equivalent (post-Brexit)

---

<a name="pillar-h"></a>
## PILLAR H — Privacy, Ethics & Regulation

**Lead pillar:** Legal / Policy
**Hypothesis contact:** H6

### Executive Summary

The product has a manageable regulatory and ethical footprint. Under the EU AI Act (Regulation (EU) 2024/1689, in force August 2024; fully applicable August 2026), a houseplant identification and substrate recommendation app almost certainly qualifies as a minimal-risk AI system — below the "limited-risk" threshold that triggers transparency obligations about AI interaction. GDPR and UK GDPR compliance is non-negotiable and centres on: EXIF metadata stripping, lawful basis for processing, user account data minimisation, and the right to erasure. The most serious ethical risk is misidentification of a toxic plant as non-toxic in a household with children or pets; this requires UX mitigation (proactive toxicity display, honest confidence) rather than legal disclaimers alone.

### Background

Privacy and AI regulation are both in active development in 2026. The EU AI Act entered into force August 2024 and its general provisions became fully applicable in August 2026 with a phased implementation — prohibited practices from February 2025, GPAI transparency requirements from August 2025, high-risk system obligations from August 2026. The GDPR and UK GDPR have been operational since 2018; the houseplant app's privacy obligations under them are relatively light but must be correctly implemented.

### State of the Literature

**EU AI Act risk classification.** The Act establishes four risk tiers: unacceptable (prohibited), high (Annex III), limited, and minimal. High-risk applications under Annex III include AI used in biometrics, critical infrastructure, education, employment, essential services, law enforcement, migration, and justice (artificialintelligenceact.eu). A plant identification and substrate recommendation app does not fall into any Annex III category. It is not a "biometric identification system" (it identifies plants, not people), not a critical infrastructure application, and not a safety component under any Annex I EU product law. The product qualifies as **minimal-risk** under the Act, meaning it faces no mandatory regulatory requirements beyond general AI literacy obligations for the deployer. However, providers of all AI systems — including minimal-risk — are advised to follow principles of human oversight, non-discrimination, and fairness (EU AI Act summary, artificialintelligenceact.eu). The EU AI Act transparency requirements for limited-risk systems (chatbots must disclose they are AI) do not apply to plant identification, but voluntary disclosure that the identification is AI-powered is good practice and pre-empts any future reclassification.

**GDPR / UK GDPR compliance.** The core obligations for this product:

- **Lawful basis:** Legitimate interest (providing the service) covers identification and recommendation if on-device. Consent is required for any off-device processing of photos or for using UGC in model training. Consent must be specific, informed, unambiguous, and easily withdrawable.
- **Data minimisation:** Collect only what is needed. If the app processes photos on-device, no photo is a "personal data processing" event. If photos are uploaded to cloud for inference or storage, they become personal data under GDPR (photos of people's homes with identifiable elements).
- **EXIF stripping:** Photos taken with a smartphone often carry GPS metadata at <10m precision. This is precise location data — "special category" adjacent. Strip EXIF at capture time before any processing or storage. The Android `ExifInterface` class handles this natively; do it before saving or transmitting the image.
- **Right to erasure:** Users must be able to delete their account and all associated data, including uploaded photos. The plant library (Room database) must have a clean deletion path. If UGC is used in model training, the association between the image and the user must be maintained (via an image ID) so deletion requests can be honoured.
- **DPA Registration:** In the UK, register with the ICO unless exempt. The app is likely exempt from mandatory registration but should confirm with legal counsel.
- **COPPA:** If the app has any pathway for users under 13, full COPPA compliance is required in the US and Google Play's Families Policy applies. The simplest approach: require users to confirm age ≥13 during onboarding and enforce it in the account creation flow.

**Google Play policies.** Relevant policies for this app:
- **Data Safety form:** Must accurately declare collection of photos (if any server-side processing), approximate location (if collected), and app activity. On-device-only apps have a very light declaration; cloud inference apps must declare photo collection.
- **AI-generated content disclosure:** Google Play requires that apps whose output includes AI-generated content disclose this. Substrate recommendations partially generated by an AI should be labelled accordingly (even if the content is rule-based and expert-curated).
- **Medical/health claims:** Plant care advice is adjacent to health claims for humans (toxicity information) and pets. Do not claim medical efficacy for air-purification benefits of plants (the NASA Clean Air Study is frequently misrepresented; see Pillar J). Plant toxicity information should be labelled as "consult a veterinarian or Poison Control" rather than a medical diagnosis.

**Misidentification harm.** The most serious ethical risk is confidently identifying a toxic plant as a non-toxic one, in a household where a child or pet subsequently ingests it. The correct mitigation stack:
1. Honest confidence display (show uncertainty, show alternatives)
2. Proactive toxicity surfacing (if ANY candidate plant in the confidence distribution is toxic, show the warning)
3. Explicit advisory disclaimer ("This is an AI identification — verify with your nursery or a local expert before relying on it for pet or child safety")
4. Poison Control and ASPCA contact information accessible from the toxicity warning screen

**Invasive and CITES-listed species.** Some commonly sold houseplants are invasive in certain jurisdictions (e.g., *Tradescantia fluminensis* in New Zealand; several *Lantana* species in Australia). CITES Appendix II includes some orchid genera, succulent *Euphorbia*, and cycads. The app should not advise propagation of invasive species or provide care advice that could facilitate establishment in the wild. A simple implementation: tag species with CITES status and regional invasive status, and display a context-sensitive note when either applies.

### Adjudication of Working Hypotheses

**H6 (liability and ethical risk is non-trivial):** SUPPORTED but manageable through UX design. The key interventions are: honest confidence display, proactive toxicity flagging, EXIF stripping, and clear advisory disclaimers. Legal exposure from disclaimers is lower than exposure from confident-wrong AI outputs.

### Implications for Design and Engineering

1. Strip EXIF data at image capture/selection time, before any processing. Use `ExifInterface` with whitelisted metadata only (orientation is the only tag worth keeping).
2. Toxicity warnings must be proactive and impossible to miss — displayed alongside any candidate plant, not buried in plant details.
3. Implement a clean, discoverable account deletion and data export flow before beta launch.
4. The app's privacy policy and data safety form must be drafted (or reviewed) by a qualified lawyer before submission.
5. A "this is an AI — not a professional opinion" disclosure should appear on the identification result screen, framed helpfully ("AI can be wrong — especially in poor lighting or with unusual plants").

### Open Questions

- At what point does providing toxicity information create a duty of care that plain advisory disclaimers cannot sever? This is the most legally uncertain question in the product; a qualified lawyer's view is needed.
- Should the app handle users in jurisdictions where specific plants are regulated (e.g., Class A weeds in New Zealand) with jurisdiction-specific notes, or adopt a global lowest-common-denominator approach?

### Recommended Further Reading

1. EU AI Act full text — artificialintelligenceact.eu
2. ICO Guidance for App Developers — ico.org.uk
3. ASPCA Animal Poison Control database
4. Google Play Data Safety requirements — play.google.com/about/developer-content-policy
5. CITES species database — cites.org/eng/resources/species.html

---

<a name="pillar-i"></a>
## PILLAR I — Market, Monetization & Growth

**Lead pillar:** Market
**Hypothesis contact:** H1 (market sizing)

### Executive Summary

The indoor plant market is large (~$20B globally in 2025, growing at ~4–5% CAGR through 2030), structurally elevated post-pandemic, and tilting online. The plant identification apps sub-market is smaller (~$175–210M in 2024) but growing faster (~14% CAGR), dominated by freemium subscription models. The user demographics strongly favour millennials and Gen Z, who are the most technically comfortable and value authenticity and transparency. The competitive gap — accurate substrate advice, honest AI, fair paywall — is real and unoccupied. The biggest growth risk is Google Lens, which provides free species identification for most common houseplants and is accessible without installation.

### Background

The houseplant market experienced a well-documented demand surge in 2020–2021 during the pandemic. By 2025–2026, search interest has normalised but remains structurally above pre-pandemic levels (Google Trends data, terrariumtribe.com 2026). The indoor plant market was valued at approximately $20–21 billion globally in 2025 across multiple analyst sources (Mordor Intelligence; Data Bridge; Zion Market Research), with North America holding ~40% of global revenue. The plant care apps market is separate and smaller, but growing faster due to digital penetration of the gardening hobby.

### State of the Literature

**Houseplant market.** Key data points synthesised across market research sources (note: estimates vary across analysts; treat as order-of-magnitude):
- Global indoor plant market: ~$20–21B in 2025; projected $29–32B by 2032–2033 at 4–5% CAGR
- Asia-Pacific: 35% of global revenue (2025), fastest growing
- North America: ~40% of global revenue; the most digitally sophisticated houseplant market
- Garden centres retain 50%+ of retail channel by value; e-commerce growing at ~10% CAGR (Mordor Intelligence 2026)
- Most popular species by retail volume: succulents/cacti, peace lily (*Spathiphyllum*), pothos (*Epipremnum*), snake plant (*Dracaena trifasciata*), monstera (*Monstera deliciosa*), rubber plant (*Ficus elastica*), spider plant (*Chlorophytum comosum*), ZZ plant (*Zamioculcas zamiifolia*), fiddle-leaf fig (*Ficus lyrata*), various *Dracaena*, orchids (*Phalaenopsis* hybrids)
- Millennial and Gen Z ownership: 25% of 18–34 year olds engaged in indoor gardening in 2018 (National Gardening Association); this figure has risen substantially post-pandemic; this demographic is the primary target user

**Plant identification app market.** Estimates vary significantly across analysts (from $175M to $1.2B in 2024 depending on the scope of "plant identification apps"). Using the Verified Market Research figure ($175.92M, 2024; narrower definition) as a lower bound and the MarketResearchIntellect figure ($1.2B, broader definition including garden apps) as an upper bound, the addressable market is substantial. Key findings:
- Freemium apps hold 76.88% of market share by revenue
- iOS holds 57% of platform revenue share; Android 43% (this gap is larger in the US than globally)
- Image recognition accounts for 75% of functionality by revenue share
- North America: 37.66% of market in 2024; highest CAGR over 5 years
- PictureThis (Glority): est. $5M/month on iOS App Store alone (Sensor Tower April 2026)

**Monetisation benchmarks.** RevenueCat State of Subscriptions reports provide the most reliable public benchmarks for lifestyle/hobby app subscription economics:
- Median Day-1 trial conversion in lifestyle apps: 1.5–3%
- Median trial-to-paid conversion: 40–60%
- Annual vs monthly pricing: annual plans see ~3x higher LTV due to lower churn
- ARPU in plant ID apps: estimated $4–6/year for free users (ad-equivalent), $25–35/year for paid subscribers based on category comparables
- Churn drivers specific to plant ID apps: wrong identifications, paywall friction, and care advice that doesn't work

**Growth strategy.** The community-to-product funnel is the most defensible growth mechanism in this category. r/houseplants has ~4.5M members; r/Monstera ~250K; r/HoyaAddicts ~150K; TikTok plant accounts with >1M followers are numerous. Authentic community engagement (not advertising) is how Planta and Greg grew. Key channels:
- SEO for houseplant care queries (high volume, low competition for substrate-specific terms)
- TikTok "repotting" content (high engagement, highly shareable)
- Partnerships with nurseries and online plant retailers (mutual referral value)
- ASO (App Store Optimization): "houseplant potting mix," "what soil for monstera," "plant identifier soil" are underserved by current category leaders

**The Google Lens risk.** Google Lens identifies the majority of common houseplants from photos, for free, with no installation. It does not provide substrate advice. The app's value proposition must therefore be: substrate and care advice is the product; identification is the unlock mechanism. This is a meaningful reframe from most competitors whose identification is the product.

### Implications for Design and Engineering

1. The product's moat is substrate advice quality, not identification. The CV pipeline enables the advice; the advice is what users pay for.
2. North American and European launch markets have structurally different substrates available (pumice vs perlite, peat-free already in UK). The recipe library must be region-aware from day one.
3. An annual subscription is strongly preferred over monthly for LTV. Offer both, but default to annual.
4. ASO must target substrate-specific queries ("best soil for pothos," "monstera potting mix"), not just generic "plant identifier" — that category is saturated.
5. Seed community presence (r/houseplants, TikTok) before launch, not after. The houseplant community is discerning and can make or break a launch.

### Open Questions

- What is the correct pricing ceiling before users defect to Google Lens + a free substrate guide website? User research needed.
- Is there an enterprise/B2B angle (nurseries, plant subscription boxes wanting a white-label advice tool)?

### Recommended Further Reading

1. AIPH Statistical Yearbook (most recent edition) — aiph.org
2. RevenueCat State of Subscriptions Report (most recent)
3. Sensor Tower / data.ai plant app category data (subscription required)
4. Google Trends — "houseplant potting mix," "monstera soil," etc. — free, directly actionable for ASO
5. Mordor Intelligence Indoor Plants Market Report (2026 edition)

---

<a name="pillar-j"></a>
## PILLAR J — Adjacent & Bonus Topics

**Lead pillar:** UX / Product
**Hypothesis contact:** N/A (roadmap pillar)

### Executive Summary

Several adjacent topics border the product's v1 scope and deserve brief synthesis because they will be raised by stakeholders, influence roadmap decisions, or quietly constrain v1 design choices. The most important: the NASA Clean Air Study (1989) is frequently cited and routinely overstated — the app must not reproduce the overstatement. Soil moisture sensors exist as a consumer product category but have largely failed commercially; the reasons are instructive for any sensor-integration roadmap. Horticultural therapy literature is well-developed and adds legitimate emotional framing for a plant care product without requiring medical claims. AR-guided capture is technically feasible with ARCore but adds significant complexity for modest image-quality gain at v1.

### Background

The brief identifies ten adjacent topics. This memo synthesises the most product-relevant, prioritised by impact on v1 decisions and roadmap plausibility.

### State of the Literature

**Indoor air quality and the NASA Clean Air Study.** The Wolverton et al. (1989) NASA study is the most cited paper in consumer plant marketing. It tested specific plants in sealed chambers and found measurable VOC (benzene, formaldehyde, trichloroethylene) reduction. What it did not demonstrate: that a typical number of houseplants in a typical room provides meaningful air quality improvement in real indoor conditions. The dose-response problem is critical: a 2019 meta-analysis by Cummings and Waring (Journal of Exposure Science & Environmental Epidemiology) calculated that air exchange rates in actual buildings are so much higher than the sealed chambers of the NASA study that you would need 10–1,000 plants per square metre to achieve the same effect. The honest communication: "Plants may contribute to indoor air quality in a small way, and they definitely contribute to mood, focus, and aesthetics — which are well-supported by separate research." Do not claim air-purification benefits in app copy without this qualification.

**Horticultural therapy and wellbeing.** The American Horticultural Therapy Association and researchers like Lee, Park, and Miyazaki have published well-replicated psychophysiological studies showing reductions in cortisol, blood pressure, and self-reported stress from interactions with plants. This is real, moderate-to-strong evidence for mood benefits of plant ownership and care. It is appropriate to reference this in product positioning ("caring for plants has been shown to reduce stress") without making medical claims. The horticultural therapy community is also a potential partnership channel (care facilities, therapy programmes).

**Bluetooth plant sensors.** The consumer plant sensor market (Xiaomi MiFlora, Parrot Flower Power, and their successors) has largely failed to achieve mainstream adoption. Post-mortem analysis from multiple sources points to: (a) Bluetooth pairing friction; (b) battery replacement cadence; (c) sensor placement sensitivity (a probe next to a rock in the pot gives misleading readings); (d) the fact that most consumer plant deaths are from overwatering, not underwatering, and sensors that alert "water now" can paradoxically increase mortality. Integration with soil sensors is not recommended for v1; a future roadmap item should require evidence of a sensor platform that has solved these UX problems before committing to integration.

**ARCore and AR-guided capture.** ARCore (Google's AR platform) supports plane detection, image tracking, and environment understanding. An AR overlay on the camera viewfinder that highlights the plant and guides the user to capture a specific organ (leaf, petiole, new growth) is technically feasible and would benefit both image quality and model performance. The engineering cost is significant (adds a substantial module, increases APK size, requires ARCore dependency). The recommendation: design the multi-shot capture UI to work well without AR in v1, with AR guidance as a v2 enhancement once the capture flow is validated.

**Community features.** The r/houseplants community (4.5M members) and analogous communities on Discord and TikTok demonstrate that plant enthusiasts actively want to discuss, share, and identify plants socially. A community feature within the app (post your plant, ask for help) is a long-term moat but a v2+ feature due to moderation costs. Key design considerations when the time comes: pseudonymous participation to protect privacy; image moderation to prevent unrelated content; specific moderation for rare plant bragging that could facilitate theft (aroid theft from botanical gardens has been traced to social media posts).

**Habit formation and care reminders.** The research on habit formation (Fogg's Tiny Habits model; Wood's *Good Habits, Bad Habits*; HCI reminder research) consistently shows that reminder pacing is critical — too-frequent reminders become noise, and users mute or delete apps that push more than 1–2 notifications per week. Watering reminders based on the substrate archetype (a succulent in a gritty mix needs far less frequent watering than a *Calathea* in a moisture-retentive mix) could be a legitimate v2 feature that the substrate recommendation engine directly enables. The care reminder frequency should be a function of archetype, pot size, season, and user-reported environment — not a fixed schedule.

### Implications for Design and Engineering

1. Never reproduce the NASA Clean Air Study claims without the dose-response qualification. The copy for any air-quality benefit must include: "in well-designed studies, though the magnitude in typical homes is likely small."
2. Sensor integration is not a v1 priority; defer until a sensor platform with solved UX emerges.
3. Design the multi-shot capture flow now to accommodate AR overlays later, but do not implement ARCore in v1.
4. Community features: plan the data model to support community posts from the beginning (plant record → optional share flag) even if the community UI ships in v2.
5. If watering reminders are added in v2, drive the frequency from the substrate archetype and pot type rather than a uniform schedule.

### Open Questions

- Is there a partnership opportunity with horticultural therapy organisations for a "wellness" positioning layer on the product?
- What is the right notification permission strategy given Android 13+ opt-in requirements and the evidence on notification fatigue?

### Recommended Further Reading

1. Cummings, B.E. & Waring, M.S., "Potted plants do not improve indoor air quality," *Journal of Exposure Science & Environmental Epidemiology*, 2019
2. Fogg, B.J., *Tiny Habits*, Virgin Books, 2020
3. American Horticultural Therapy Association publications (ahta.org)
4. ARCore documentation — developers.google.com/ar
5. Lee, M.S. et al., "Interaction with indoor plants may reduce psychological and physiological stress," *Journal of Physiological Anthropology*, 2015

---

<a name="iim"></a>
# IMPLEMENTATION IMPLICATIONS MEMO (IIM)

**Document:** Implementation Implications Memo v1.0
**Date:** 11 May 2026
**Audience:** Design and Engineering teams
**Authors:** Research synthesis team

---

## IIM Section 1: What Plant Do We Support?

**Decision: Target 150–200 species across ~45 genera for v1.**

The market evidence (Pillar A, Pillar I) consistently shows that roughly 150–200 species dominate global houseplant retail. A database of this scope is curated at human scale, can be built with high-quality substrate annotations by 3–5 horticultural experts in 8–12 weeks, and covers >90% of the plants users will photograph. The long tail of collector/specialty plants (rare aroids, collector cacti, uncommon hoyas) can be added iteratively; the identification model can return "I'm not sure — is this in the Philodendron family?" for out-of-distribution inputs.

**Key species list seed (to be expanded by horticulture lead):**
Aroids: *Monstera deliciosa, M. adansonii, Philodendron hederaceum, P. gloriosum, Epipremnum aureum, Scindapsus pictus, Syngonium podophyllum, Spathiphyllum wallisii, Anthurium andraeanum, Aglaonema* spp., *Alocasia* spp., *Zamioculcas zamiifolia*

Succulents/Xerophytes: *Echeveria* spp., *Crassula ovata, Aloe vera, Haworthiopsis attenuata, Gasteria* spp., *Sansevieria* (= *Dracaena trifasciata, D. angolensis*), most common Cactaceae

Ficus/foliage: *Ficus lyrata, F. elastica, F. benjamina, Dracaena fragrans, D. marginata, Chlorophytum comosum, Calathea ornata, Goeppertia orbifolia, Maranta leuconeura, Begonia* spp.

Hoya/climbers: *Hoya carnosa, H. kerrii, H. pubicalyx*

Orchids: *Phalaenopsis* hybrids (a single "Phalaenopsis hybrid" category covers retail adequately)

Other: *Fittonia albivenis, Peperomia obtusifolia, P. caperata, Pilea peperomioides, Tradescantia zebrina, Oxalis triangularis, Pachira aquatica, Fatsia japonica*

---

## IIM Section 2: What Model Do We Ship?

**Decision: FastViT-S12 or MobileViT-S, fine-tuned on a purpose-collected 150-species indoor houseplant dataset; INT8-quantized for LiteRT deployment; Play Asset Delivery (install-time).**

The PlantCLEF literature (Pillar C) establishes ViT superiority for plant classification. FastViT-S12 (Apache 2.0 license; ~30MB INT8) achieves sub-300ms inference on a Snapdragon 720G-class SoC with the LiteRT GPU delegate — within the target user experience window. Fine-tuning on a purpose-collected indoor dataset is non-negotiable; the domain gap between outdoor botanical datasets and indoor houseplant photography is large enough to substantially degrade performance without it.

**Training data strategy:**
- Base: PlantNet-300K (CC BY-SA) + iNaturalist CC BY records filtered to target 150 species — provides ~2,000–5,000 images/species for common species
- Purpose-collected: 500–2,000 images per species, photographed indoors under realistic consumer conditions (mixed lighting, decorative pots, partial views), sourced via nursery partnerships + paid crowdsourcing
- Augmentation: aggressive colour jitter (to handle indoor lighting variation), random crop (partial views), random rotation, MixUp/CutMix
- Post-training calibration: temperature scaling per-class; target ECE <0.05 on held-out indoor test set
- Open-set: max-softmax threshold at 0.4 (calibrated); returns "I don't recognise this plant — try a different angle" below threshold

**Cloud fallback:** Implement optional cloud inference for queries where on-device confidence <0.4. Cloud path sends only the cropped plant image (EXIF-stripped); returns top-3 candidates with confidence. Presented as an opt-in: "Try harder (requires internet)." Data Safety form must declare this if shipping.

---

## IIM Section 3: What Substrate Do We Recommend?

**Decision: 9-archetype rule-based recommendation engine; knowledge base in Room/SQLite; region-aware ingredient variants; peat-light from day one.**

The substrate science literature (Pillar B) strongly validates the 9-archetype schema. The recommendation logic is a lookup: identification → ecological strategy → archetype → recipe → regional variant. The nine archetypes (standard houseplant, aroid chunky, moisture-retentive, succulent gritty, cactus mineral, epiphytic bark, semi-hydro LECA, ericaceous acid, carnivorous) cover the full product scope.

**Confidence routing:**
- High confidence (calibrated P > 0.70) → direct recommendation
- Split confidence between same-archetype candidates → direct recommendation + "this may also be X, which uses the same substrate"
- Split confidence between different-archetype candidates → disambiguation question ("Does the plant have stiff, upright leaves or soft, spreading leaves?")
- Low confidence (< 0.40) → no recommendation until identification improved

**Recipe format (per archetype):**
Each archetype record contains: archetype name (plain English), one-sentence ecology explanation, base recipe (ingredient + fraction + purpose), UK peat-free variant, North American variant, "big-box simplified" version, AFP and pH range, watering interval indication ("allow top 2cm to dry between waterings"), pot type guidance, and repotting frequency.

**Regional availability:**
Ingredients are tagged with regional availability: "Global" (perlite, coco coir), "UK/EU" (fine grit, John Innes-style mixes), "US" (Miracle-Gro Perlite, orchiata bark via Amazon), "specialist only" (akadama, premium pumice). Recipes dynamically substitute based on user's region setting.

---

## IIM Section 4: How Do We Present Confidence?

**Decision: Show top-2 candidates with distinguishing features, not a percentage. Low-confidence returns a disambiguation prompt, not a guess.**

The HCI literature (Pillar E, Pillar F) is clear: numerical probabilities mislead lay users; ranked alternatives with visual/textual discriminators are both more informative and more trusted. The Pl@ntNet and Seek apps demonstrate this pattern in production.

**Confidence UX pattern:**
```
IDENTIFICATION RESULT

Most likely: Monstera deliciosa (Fenestrated Monstera)
Distinctive feature: Large splits and holes in mature leaves

This could also be: Monstera adansonii (Swiss Cheese Plant)
Distinctive feature: Smaller leaves with complete holes, no splits

[Tap to confirm your plant ▼]
[Not sure? Take another photo of a leaf close-up →]
```

**For low-confidence returns:**
```
I'M NOT SURE

The photo might show a Philodendron or an Epipremnum — they look very similar.

To help me tell them apart, can you:
[Take a photo of a leaf petiole →]
[Show me the new growth →]
[Choose from a visual guide →]
```

This pattern is derived from Pillar C (calibrated models), Pillar E (UX literature), and Pillar F (recommendation confidence propagation).

---

## IIM Section 5: How Do We Handle Privacy?

**Decision: On-device-first, EXIF stripped at capture, no user account required for v1, cloud fallback opt-in with clear disclosure.**

The GDPR analysis (Pillar H) supports a privacy architecture that minimises data collection. The app can function fully offline for the core identification + recommendation loop. User accounts are optional and needed only for plant library persistence across devices. If no account exists, plant records are stored locally (Room) with no server communication.

**EXIF stripping:** Every image captured or selected from the gallery must have EXIF metadata stripped using `ExifInterface` before any processing. Retain only orientation (for correct display). This is a single function call; implement it as the first step in the image preprocessing pipeline and test it before launch.

**Data Safety form declaration:**
- Photos/videos: "Not collected" if fully on-device. "Collected — not shared with third parties — encrypted in transit" if cloud fallback is shipped.
- Approximate location: "Not collected" unless a location-based feature is added.
- App activity: "Collected" for crash reports and analytics; standard Crashlytics declaration.

---

## IIM Section 6: What Do We Paywall?

**Decision: Free — identification (5/day), basic substrate recommendation, toxicity information. Paid — unlimited identification, full recipe detail with sourcing guide, plant library sync, seasonal repotting calendar.**

The monetisation literature (Pillar I, Pillar E) shows that paywalling the identification result is the primary driver of negative reviews and uninstalls. The correct placement: deliver the value (identification + one-paragraph recommendation), then offer the expansion (full recipe + sourcing + calendar) behind the paywall.

**Subscription structure:**
- Free tier: 5 identifications/day, one-sentence substrate recommendation, toxicity flag, species info
- Premium tier (~£2.99/month or £19.99/year): unlimited identifications, full recipe (all variants + substitutions), sourcing guide (what to buy in your region), seasonal repotting calendar, plant library sync across devices
- One-time purchase option (~£7.99): full recipe library unlock, no subscription — addresses the user cohort who strongly objects to subscriptions

---

## IIM Section 7: What Are the Top Risks?

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Misidentification of toxic plant as non-toxic | Medium | High | Proactive toxicity warning for all candidates in distribution, not just top-1 |
| User-collected training data creates GDPR obligation missed at launch | Medium | High | Implement data-subject deletion pipeline before any model training on UGC |
| Peat-heavy recipe library looks dated within product lifetime | High | Medium | Peat-free variants required for every archetype from day one |
| CC BY-SA training data creates share-alike obligation on model weights | Low–Medium | High | Legal opinion required before public release on PlantNet-300K-trained models |
| Google Lens commoditises identification layer | High | Medium | Differentiate on substrate advice quality, not identification; paywall the advice |
| On-device model accuracy too low on mid-range Android | Medium | High | Test on Snapdragon 720G class devices before launch; implement cloud fallback |
| NNAPI deprecation breaks GPU acceleration on Android 15 | High | Low | Migrate to LiteRT GPU delegate API; NNAPI fallback removed by design |
| EU AI Act reclassification of app as limited-risk | Low | Low | Document the minimal-risk assessment; follow voluntary transparency principles |
| Offensive common names in copy | Medium | Medium | Review all 200 species names against RHS and BGCI guidance before launch |
| Fake/LLM-generated plant care content indexed in competitive landscape | Low | Low | Our moat is T1/T2-sourced expertise, clearly attributed |

---

## IIM Section 8: What Do We Validate in User Research?

1. **Confidence display test:** Does top-2 ranked alternatives outperform numerical probability (%) on user trust and correct action rate? (A/B test with 200 users, 2 variants)
2. **Progressive disclosure test:** 4-layer recommendation vs. single recipe card — which drives more "followed recommendation" self-reports?
3. **Camera flow test:** Single-shot vs. multi-shot (whole plant + leaf) — does multi-shot produce meaningfully better identification accuracy in realistic conditions?
4. **Paywall placement test:** Block on result vs. block on recipe expansion — which produces higher conversion without increasing Day-1 uninstall rate?
5. **Toxicity warning placement:** Inline on identification result vs. separate tab — which produces higher "I saw the toxicity warning" recall in follow-up survey?

---

## IIM Section 9: Adjudication of All Working Hypotheses

| Hypothesis | Verdict | Confidence |
|---|---|---|
| H1: Small core species set (~200) | **SUPPORTED** | High — consistent across market, retail, and botanical literature |
| H2: Finite archetype library (~9 archetypes) | **STRONGLY SUPPORTED** | High — the substrate science literature converges on this schema |
| H3: On-device CV sufficient | **CONDITIONALLY SUPPORTED** | Medium — sufficient for top-100 species in good conditions; cloud fallback recommended for tail |
| H4: Deployment distribution much worse than research data | **STRONGLY SUPPORTED** | High — domain gap is well-documented; purpose-collected indoor training data is essential |
| H5: Cultivar misidentification is main failure mode | **PARTIALLY SUPPORTED** | Medium — genus-level misidentification is at least as dangerous; cultivar confusion is real but not the primary care risk |
| H6: Liability risk is non-trivial | **SUPPORTED but mitigable** | High — mitigated by UX design (honest confidence, proactive toxicity), not disclaimers alone |

---

## IIM Section 10: Recommended Launch Sequencing

**Phase 1 (MVP, months 1–9):**
- 100 top-species identification model (on-device, FastViT-S12)
- 9-archetype rule-based recommendation engine
- UK and North America recipe variants (peat-free by default in UK)
- Toxicity database integrated and displayed
- Basic plant library (local, no sync)
- Core paywalled feature: full recipe with sourcing guidance
- Privacy: on-device only, no user account required

**Phase 2 (months 10–18):**
- Expand to 200 species
- Cloud fallback inference (opt-in)
- Plant library sync (optional account)
- Seasonal repotting calendar
- Watering interval guidance (archetype-driven)
- Community identification assist ("Ask the community")

**Phase 3 (months 19+):**
- Care reminder system (archetype + pot type + season driven)
- AR-guided capture
- Integrate commercial substrate product ratings
- Sensor integration if a viable platform emerges
- Multi-language support

---

<a name="sources"></a>
# ANNOTATED SOURCE LIST

*Minimum 60 core sources across all pillars. Full library should be maintained in Zotero with complete bibliographic metadata. This represents the curated seed for that library.*

---

## A — Botany & Taxonomy (Tier T1–T2)

**A01.** Plants of the World Online (POWO). Royal Botanic Gardens, Kew. https://powo.science.kew.org
*Tier: T2 (authoritative reference). Pillar: A.*
The canonical source for current accepted species names and synonyms across all vascular plants. Updated continuously. All product database keys should be validated against POWO. Key use case for this project: resolving Sansevieria → Dracaena synonymy; verifying Monstera, Philodendron, and Epipremnum/Scindapsus boundaries. *C1: Sansevieria trifasciata is a synonym of Dracaena trifasciata (Prain) Mwachala (accepted 2019). C2: Epipremnum aureum is the accepted name; Pothos aureus and Scindapsus aureus are synonyms. I1: Product database must include synonym resolution for all 200 species.*

**A02.** Mabberley, D.J. *Mabberley's Plant-Book: A Portable Dictionary of Plants, their Classification and Uses*, 4th ed. Cambridge University Press, 2017.
*Tier: T2. Pillar: A.*
Single-volume reference covering all genera. Authoritative for genus-level etymology, family placement, and species counts. Updated for APG IV. *C1: Zamioculcas is placed in Araceae, tribe Zamioculcadeae. C2: Numerous former Sansevieria species are now listed under Dracaena. I1: Use as cross-check for POWO entries.*

**A03.** Chase, M.W. et al. (APG IV). "An update of the Angiosperm Phylogeny Group classification for the orders and families of flowering plants: APG IV." *Botanical Journal of the Linnean Society* 181 (2016): 1–20.
*Tier: T1. Pillar: A.*
The primary paper for the current consensus angiosperm classification. Directly responsible for the Sansevieria/Dracaena synonymy and other nomenclatural changes affecting retail houseplants. *C1: Dracaenaceae is subsumed into Asparagaceae. C2: APG IV formally consolidates Sansevieria within Dracaena. I1: Pre-2017 care books use outdated taxonomy; all names must be cross-checked.*

**A04.** International Aroid Society. *Aroideana* (journal). Ongoing.
*Tier: T5. Pillar: A.*
The primary specialist publication for Araceae taxonomy and culture. Articles by Tom Croat (Missouri Botanical Garden) and Ecuagenera on Monstera, Philodendron, and Anthurium systematics provide the deepest available guidance for the most commercially important houseplant family. *C1: Over 50% of the top 50 retail houseplant species are Araceae. I1: Subscribe to Aroideana for substrate and identification guidance specific to aroids.*

**A05.** ASPCA Animal Poison Control Centre. Toxic and Non-Toxic Plants database. aspca.org/pet-care/animal-poison-control/toxic-and-non-toxic-plants
*Tier: T4. Pillar: A, H.*
The most widely used and regularly updated consumer-facing toxicity database. Covers cats, dogs, and horses. Notably: all common aroids (Monstera, Philodendron, Epipremnum, Spathiphyllum) are listed toxic to cats and dogs due to calcium oxalate. Lilium species are listed as severely toxic to cats. *C1: Monstera deliciosa is toxic to dogs and cats (oral irritation, not fatal). C2: All Lilium species are potentially fatal to cats through renal failure. I1: Toxicity warning UX must distinguish "oral irritant" from "potentially fatal" — these require different urgency levels.*

**A06.** Missouri Botanical Garden. Plant Finder. missouribotanicalgarden.org/plantfinder
*Tier: T4. Pillar: A.*
Practical per-species care information, including soil preferences, light requirements, and hardiness. Well-maintained; less detailed on substrate composition than T1 sources but useful for rapid baseline care facts. *C1: Provides USDA hardiness zone information for outdoor/borderline species. I1: Use as supplementary reference for care data not covered by specialist literature.*

---

## B — Substrate Science (Tier T1–T3)

**B01.** Raviv, M. & Lieth, J.H. (eds). *Soilless Culture: Theory and Practice*, 2nd ed. Elsevier, 2019.
*Tier: T2. Pillar: B.*
The textbook for container substrate science. Covers physical characterisation methods, chemical properties, ingredient science, and practical application. The definitive reference for AFP, container capacity, and pH management sections of the substrate archetypes. *C1: Optimal AFP for most container crops is 10–30% by volume measured at -1 kPa. C2: The perched water table phenomenon means drainage holes do not prevent anaerobic conditions in the bottom substrate zone. I1: All archetype AFP specifications in the product database should be validated against ranges stated in this text.*

**B02.** De Boodt, M. & Verdonck, O. "The Physical Properties of the Substrates in Horticulture." *Acta Horticulturae* 26 (1972): 37–48.
*Tier: T1. Pillar: B.*
The foundational paper for modern container substrate physics. Introduced the standard definitions of AFP, container capacity, and easily available water that remain in use. Cited by virtually all subsequent substrate characterisation work. *C1: AFP is defined as the volumetric fraction of air present at -1 kPa matric potential (after free drainage in a standard container). C2: Easily available water is the water released between -1 kPa and -5 kPa. I1: Any archetype recipe claiming a specific AFP should specify measurement conditions consistent with this standard.*

**B03.** Bilderback, T.E., Warren, S.L., Owen, J.S., and Albano, J.P. "Healthy Substrates Need Physicals Too!" *HortTechnology* 15.4 (2005): 747–751.
*Tier: T1. Pillar: B.*
One of the North Carolina State University substrate series papers. Provides practical physical test methods for container media and benchmark values for common commercial mixes. *C1: Standard commercial pine bark:perlite (3:1 v/v) mix achieves AFP of 15–22%. C2: Bulk density >0.5 g/cm3 indicates a media too heavy for container use. I1: Bench-top physical testing methods described here are accessible to a small horticulture team and should be used to validate archetype recipe AFP claims.*

**B04.** Frontiers in Horticulture. "The persistent challenge of alternatives to peat in container-based horticulture: a historical review of the field of growing media." 2025.
*Tier: T1. Pillar: B.*
Recent comprehensive review of 60 years of peat alternative research. Documents why replacing peat has been harder than expected: composted bark and green compost perform worse on germination in controlled trials; user surveys show frustration. Identifies coco coir as the most adopted alternative. Provides lifecycle assessment comparisons. *C1: Life-cycle assessments show bio-based peat alternatives reduce CO2 emissions by 89–109% vs peat when substituted on a volume basis. C2: Surveys across Northern Europe document germination failure and poor growth with early peat-free mixes. I1: Recipes should use coco coir as the primary organic component (not peat) and should be tested for AFP performance before publication.*

**B05.** UK Government. Ban on Peat Use in Retail Bagged Growing Media (England). 2024.
*Tier: T4. Pillar: B.*
The UK regulation banning retail peat sales in bagged growing media from 2024, with professional use restrictions from 2026. The definitive source for the regulatory timeline. *C1: Retail sale of peat-containing growing media in England is banned from 2024. C2: Some professional uses (rootball plants, mushroom cultivation) are exempt. I1: The app's UK recipe variants must be fully peat-free immediately; professional alternatives must follow 2026 restrictions.*

**B06.** Argo, W.R. & Fisher, P.R. *Understanding pH Management for Container-Grown Crops.* Meister Media Worldwide / Michigan State University, 2002.
*Tier: T3. Pillar: B.*
The most accessible technical reference for pH management in container horticulture. Covers substrate pH, fertilizer acidification, alkalinity in irrigation water, and crop response. Required reading for the chemistry dimensions of archetype specification. *C1: Most container crops perform best at substrate pH 5.5–6.5; ericaceous crops require 4.5–5.5. C2: Coco coir buffers at 5.5–6.5 and can require lime addition when replacing peat (which is inherently acidic) to avoid excessive pH rise. I1: Every archetype record must include a recommended pH range and a note on lime/acidifier addition when mixing.*

**B07.** Premier Tech. PRO-MIX Technical Information Series. premier-horticulture.com
*Tier: T3. Pillar: B.*
A substantial library of technical bulletins from one of the world's largest professional growing media manufacturers. Covers AFP measurement, peat grades, perlite grades, coco coir properties, and practical mix formulation. Technically careful despite commercial source. *C1: Coarse-grade perlite (3–6mm) contributes ~10–15% AFP per 20% volume addition to a standard coir-based mix. C2: Wetting agent addition reduces the matric potential threshold at which dried coir re-wets from approximately -15 kPa to -3 kPa. I1: Wetting agent recommendation for coir-heavy mixes should be flagged in archetype guidance.*

---

## C — Computer Vision & Plant ID (Tier T1)

**C01.** Garcin, C., Joly, A., Bonnet, P., et al. "Pl@ntNet-300K: A Plant Image Dataset with High Label Ambiguity and a Long-Tailed Distribution." *NeurIPS Datasets and Benchmarks Track*, 2021.
*Tier: T1. Pillar: C.*
The primary paper for the PlantNet-300K dataset: 306,146 images, 1,081 species, Creative Commons licensed. Carefully documents label ambiguity and long-tail distribution. The best available open dataset for initial training; its European flora bias is well-characterised. *C1: ~55% of species in PlantNet-300K have fewer than 100 images; the long tail is very thin. C2: Inter-annotator agreement at species level is ~85% in the dataset. I1: PlantNet-300K should be used for pretraining; purpose-collected indoor data is needed for fine-tuning.*

**C02.** Goëau, H., Bonnet, P., Joly, A. "Overview of PlantCLEF 2023: Image-Based Plant Identification at Global Scale." *Working Notes of CLEF 2023*. arXiv:2509.17622.
*Tier: T1. Pillar: C.*
Documents the 2023 PlantCLEF challenge results including architecture comparison. Key finding: EVA ViT achieved MA-MRR 0.674 vs CNN best at 0.618 on 80,000-species task. *C1: Vision transformer approaches outperform CNN approaches by a meaningful margin on large-scale plant identification. C2: Self-supervised ViT pretraining (DINO, MAE) improves performance over supervised CNN pretraining of equivalent compute. I1: Fine-tune from a ViT backbone, not a pure CNN, for the identification model.*

**C03.** PlantCLEF 2024 Overview. arXiv:2509.15768 (September 2025).
*Tier: T1. Pillar: C.*
2024 challenge introduced multi-species vegetation quadrat identification. Training set of 1.7M individual plant images provided alongside state-of-the-art ViT pretrained models. *C1: 1.7M images across 7806+ species in the training set; the Pl@ntNet ViT pretrained on this data is available. C2: Multi-scale tiling strategies substantially improved quadrat identification performance. I1: The Pl@ntNet pretrained ViT released for PlantCLEF 2024 is a strong starting backbone for fine-tuning.*

**C04.** Guo, C., Pleiss, G., Sun, Y., and Weinberger, K.Q. "On Calibration of Modern Neural Networks." *ICML 2017*.
*Tier: T1. Pillar: C.*
The canonical calibration paper. Shows that modern deep nets are systematically overconfident; introduces temperature scaling as a simple post-hoc calibration method. *C1: ResNet and DenseNet models achieve near-perfect accuracy on ImageNet but ECE of 0.10–0.15 (highly miscalibrated). C2: Temperature scaling reduces ECE to <0.02 with minimal accuracy loss. I1: Implement temperature scaling before deployment; calibrate on an indoor houseplant held-out set.*

**C05.** Geng, C., Huang, S., and Chen, S. "Recent Advances in Open Set Recognition: A Survey." *IEEE TPAMI* 43.10 (2021): 3614–3631.
*Tier: T1. Pillar: C.*
Comprehensive survey of open-set recognition methods. Covers OpenMax, energy-based OSD, Mahalanobis distance approaches, and recent deep learning methods. *C1: Energy-based out-of-distribution detection outperforms pure softmax thresholding for most FGVC tasks. C2: Threshold calibration for OSD methods requires a separate held-out OOD dataset (e.g., random indoor objects). I1: Build a small "non-plant" test set (home objects, pets, walls) for calibrating the OSD threshold.*

**C06.** Vasu, P.K.A., Gabriel, J., Zhu, J., et al. "FastViT: A Fast Hybrid Vision Transformer using Structural Reparameterization." *ICCV 2023*.
*Tier: T1. Pillar: C, D.*
FastViT is currently the most accurate architecture in the "mobile ViT" class at equivalent latency. Apache 2.0 license; TFLite-compatible. *C1: FastViT-S12 achieves 79.8% top-1 on ImageNet at 3.8ms on iPhone 14 (roughly comparable to ~300ms on Snapdragon 720G). C2: Structural reparameterisation reduces inference-time parameters with no accuracy loss vs training-time. I1: FastViT-S12 is the recommended starting architecture; validate latency on target Android SoC range.*

**C07.** Mehta, S. & Rastegari, M. "MobileViT: Light-Weight, General-Purpose, and Mobile-Friendly Vision Transformer." *ICLR 2022*.
*Tier: T1. Pillar: C, D.*
Introduces MobileViT, a hybrid CNN-ViT architecture designed for mobile deployment. Achieves better accuracy than MobileNetV3 and EfficientNet-Lite at equivalent parameter counts. *C1: MobileViT-S (5.7M parameters) achieves 78.4% top-1 on ImageNet at <30MB model size. C2: MobileViT significantly outperforms pure CNN baselines on fine-grained recognition tasks due to global attention. I1: MobileViT-S is a viable fallback if FastViT shows LiteRT compatibility issues.*

**C08.** Radford, A., Kim, J.W., Hallacy, C., et al. "Learning Transferable Visual Models From Natural Language Supervision." *ICML 2021*. (CLIP)
*Tier: T1. Pillar: C.*
The CLIP paper. Multimodal contrastive learning on 400M image-text pairs produces a vision encoder with strong zero-shot capabilities. *C1: CLIP achieves 76.2% zero-shot top-1 on ImageNet without task-specific training. C2: CLIP-based models show strong zero-shot capability on fine-grained biological taxa when training includes natural language botanical descriptions. I1: A CLIP-based zero-shot baseline should be benchmarked against the fine-tuned classification model for rare species in the long tail.*

---

## D — Android Engineering (Tier T2–T4)

**D01.** Android Developers Documentation. *Guide to App Architecture.* developer.android.com/topic/architecture. Current.
*Tier: T4 (authoritative vendor documentation). Pillar: D.*
Google's official Modern Android Development (MAD) architecture guidance. The definitive reference for the recommended MVVM + Unidirectional Data Flow pattern with Jetpack libraries. *C1: Google recommends a data layer → domain layer → UI layer architecture with unidirectional data flow for all new Android apps. C2: Compose state-hoisting is the recommended pattern for UI state management. I1: Architecture decisions should align with MAD guidance to maximise use of Jetpack libraries and hire-ability of future Android engineers.*

**D02.** Google. *Now in Android*. github.com/android/nowinandroid. Current.
*Tier: T4. Pillar: D.*
Google's open-source reference implementation of the MAD stack: Kotlin, Compose, Hilt, Room, Baseline Profiles, modular architecture. The canonical implementation reference for the engineering team. *I1: Clone and study the Now in Android architecture before implementing any structural decisions for the app.*

**D03.** Android Developers. *LiteRT (formerly TensorFlow Lite) Documentation.* ai.google.dev/edge/litert. Current.
*Tier: T4. Pillar: D.*
The official documentation for on-device ML inference on Android and other platforms. Covers GPU delegate, CPU fallback, model optimisation (INT8 quantization), and the Play Asset Delivery integration. *C1: NNAPI is deprecated in Android 15 (API 35); LiteRT GPU delegate is the recommended path for hardware-accelerated inference. C2: Post-training INT8 quantization reduces model size by ~4x with typical accuracy degradation of <1% for classification tasks. I1: Implement GPU delegate with CPU fallback; do not call NNAPI directly.*

**D04.** Android Developers. *CameraX Documentation.* developer.android.com/training/camerax. Current.
*Tier: T4. Pillar: D.*
CameraX is the Jetpack library for camera capture, providing a stable API across the Android device ecosystem. Critical for this app's primary interaction mode. *C1: CameraX ImageAnalysis use case provides frames as ImageProxy in YUV_420_888; conversion to RGBA Bitmap must be implemented efficiently to avoid frame drops. C2: The ImageCapture use case and ImageAnalysis use case cannot both be used at maximum resolution simultaneously on most mid-range devices. I1: Use preview + image analysis (downsampled) for live identification guidance; use full-resolution image capture only when user taps "identify."*

**D05.** Moskała, M. *Kotlin Coroutines: Deep Dive.* Kt. Academy Press, 2022.
*Tier: T2 (authoritative book). Pillar: D.*
The most comprehensive technical reference on Kotlin coroutines. Essential background for the engineering team on the concurrency primitives used throughout the Jetpack stack. *I1: The ML inference pipeline must be structured as a coroutine with `Dispatchers.Default` for compute; UI state updates must be in `Dispatchers.Main`. Avoid blocking the main thread at any point.*

---

## E — UX, Product & Competitive Field (Tier T2–T4)

**E01.** Google PAIR. *People + AI Research Guidebook.* pair.withgoogle.com/guidebook. 2019 (updated).
*Tier: T2. Pillar: E, F.*
Google's practical design guidance for AI-powered products. Covers: how to communicate AI uncertainty, how to design for user feedback, how to handle errors. The most directly applicable reference for the identification result screen design. *C1: Show 2–3 alternatives rather than a single answer for AI classification outputs; users who see alternatives report higher trust even when the top result is correct. C2: Provide a clear pathway for user correction of AI errors; apps that don't lose user trust rapidly when the AI is wrong. I1: Design the identification result screen per these guidelines: top-2 candidates with distinguishing features, correction pathway, honest uncertainty language.*

**E02.** Amershi, S., Weld, D., Vorvoreanu, M., et al. "Guidelines for Human-AI Interaction." *CHI 2019*. Microsoft Research.
*Tier: T1. Pillar: E.*
18 guidelines for designing human-AI interaction across the product lifecycle. The HAX Toolkit is built from these guidelines. *C1: Guideline 7 — "Support efficient invocation" — apps should allow users to trigger AI assistance with minimal friction (one tap, not multiple steps). C2: Guideline 11 — "Make clear why the AI did what it did" — brief rationale increases user trust and correct use. I1: Every identification result must include a one-line "why I think this is X" explanation.*

**E03.** Material Design 3 Documentation (Google). m3.material.io. Current.
*Tier: T4. Pillar: D, E.*
The current Android platform design language, including Material You dynamic colour. Essential reference for all UI design decisions. *I1: Use Material 3 tokens throughout Compose UI; do not hardcode colours — use dynamic colour to respect system theme.*

**E04.** Sensor Tower. *PictureThis App Performance Data, April 2026.* sensortower.com
*Tier: T4 (commercial analytics). Pillar: E, I.*
PictureThis iOS App Store performance data: estimated 700K downloads and $5M revenue in the most recent full month. *C1: PictureThis generates approximately $5M/month on iOS alone, implying substantial Android revenue on top. C2: The app maintains a high App Store rating despite negative review sentiment through review volume. I1: The total addressable revenue in the plant ID category is substantial; a premium substrate-advice positioning at lower paywall friction is commercially viable.*

---

## F — Recommendation Logic (Tier T1–T2)

**F01.** Ricci, F., Rokach, L. & Shapira, B. (eds). *Recommender Systems Handbook*, 3rd ed. Springer, 2022.
*Tier: T2. Pillar: F.*
Comprehensive handbook covering content-based, collaborative-filtering, and hybrid recommender approaches. Directly relevant for the knowledge-base design and the decision about when (not) to use collaborative filtering. *C1: Content-based systems outperform collaborative filtering in domains with small user bases and well-defined item attributes — exactly the substrate recommendation domain. C2: Cold-start problem (new species with no ratings) is inherently solved by a rule-based content system. I1: Ship a rule-based content system for v1; design the schema to add a collaborative layer when outcome data is available.*

**F02.** Aggarwal, C.C. *Recommender Systems: The Textbook.* Springer, 2016.
*Tier: T2. Pillar: F.*
Textbook covering matrix factorization, knowledge-based systems, and hybrid methods. Knowledge-based recommenders chapter is directly applicable. *C1: Knowledge-based recommenders are appropriate when items have well-defined attributes and utility can be specified a priori. C2: Knowledge-based systems do not suffer from cold-start or popularity bias. I1: Structure the plant knowledge base as a constraint-based recommender: plant attributes → substrate requirements → archetype mapping.*

---

## G — Data & IP (Tier T3–T4)

**G01.** Creative Commons. License documentation. creativecommons.org/licenses. Current.
*Tier: T4. Pillar: G.*
The authoritative reference for CC license terms. *C1: CC BY-NC licenses explicitly exclude commercial use — including in ad-supported or subscription apps. C2: CC BY-SA requires derivative databases to carry the same license; the application to trained model weights is legally unsettled. I1: Screen all training data sources for CC BY-NC; exclude those records. Seek legal opinion on CC BY-SA model weights.*

**G02.** GBIF. Data Use Agreement and Citation Guidelines. gbif.org/citation-guidelines. Current.
*Tier: T4. Pillar: G.*
GBIF requires citation of the data portal and individual datasets used for any publication or product. Commercial use is permitted for CC0 and CC BY records. *C1: GBIF requires a formal data citation in any product or publication using GBIF-mediated data. C2: Many GBIF-mediated datasets from European institutions carry CC BY-SA at the dataset level. I1: Access GBIF data via API with per-record license filtering (CC0, CC BY only) and maintain a data citation log.*

**G03.** Data Provenance Initiative. *Consent in Crisis: The Rapid Decline of the AI Training Data Consent Framework.* 2023. dataprovenance.org
*Tier: T3. Pillar: G.*
Large-scale audit of licenses across 1,800+ AI training datasets. Showed that many popular AI datasets contain data that restricts commercial use when licensing is examined carefully. *C1: Over 25% of widely used AI training datasets contain data with non-commercial license restrictions. C2: License drift (datasets changing terms post-curation) is a documented risk. I1: Maintain a formal data provenance log with license source, date accessed, and license version. Check annually.*

---

## H — Privacy, Ethics & Regulation (Tier T3–T4)

**H01.** EU AI Act. Regulation (EU) 2024/1689 of the European Parliament and of the Council. Official Journal of the EU, July 2024.
*Tier: T3 (primary legislation). Pillar: H.*
The full text of the EU AI Act. Risk classification framework: unacceptable → prohibited; high risk → Annex III obligations; limited risk → transparency; minimal risk → no mandatory requirements. *C1: Annex III high-risk categories do not include consumer plant identification or care advice applications. C2: AI Act transparency rules for limited-risk systems are applicable to chatbots; a plant ID model is not a chatbot but voluntary AI disclosure is recommended. C3: Full applicability from August 2026. I1: Document the minimal-risk assessment in writing before EU launch; follow voluntary transparency principles to future-proof against reclassification.*

**H02.** Information Commissioner's Office (ICO). Guidance for App Developers. ico.org.uk/for-organisations/
*Tier: T4. Pillar: H.*
UK data protection guidance specific to app developers. Covers lawful basis, children's privacy, and the right to erasure. *C1: Apps that process photos for analysis are processing personal data if those photos could identify individuals (home environment, faces). C2: The right to erasure under UK GDPR must be technically implemented, not just promised in the privacy policy. I1: Implement a full account deletion and data purge pipeline before beta launch.*

**H03.** Google Play. Data Safety Form Requirements and Developer Policy Centre. play.google.com/about/developer-content-policy. Current.
*Tier: T4. Pillar: D, H.*
The policy requirements for all apps on Google Play, including Data Safety declarations. *C1: Apps using photos for on-device processing only (not transmitted) can declare "photos not collected" in Data Safety. C2: AI-generated content in apps requires disclosure per Play policy. I1: Complete and legally review the Data Safety form before first submission; wrong declarations can result in app removal.*

---

## I — Market (Tier T3–T4)

**I01.** Mordor Intelligence. *Indoor Plants Market Size, Trends, Growth Analysis & Global Report.* January 2026.
*Tier: T3. Pillar: I.*
Market sizing report: global indoor plants market $13.12B (2025 conservative estimate) to $20.68B (broader definition), growing at 3.75–4.87% CAGR. Asia-Pacific 35% of global revenue. *C1: Asia-Pacific commands 35.4% of global indoor plant market revenue as of 2025. C2: Online platforms are growing at 10.05% CAGR — significantly faster than the overall market. I1: v1 product should be optimised for North American and UK markets but designed for APAC expansion.*

**I02.** Verified Market Research. *Plant Identification Apps Market.* March 2025.
*Tier: T3. Pillar: I, E.*
Market sizing for plant ID apps: $175.92M in 2024, projected $519.07M by 2032 at 14.56% CAGR. Freemium model holds 76.88% market share. *C1: Freemium apps dominate plant ID app revenue. C2: Image recognition is the primary functionality by revenue (75.65% market share). I1: Freemium subscription is the correct commercial model; one-time purchase should be a secondary option.*

**I03.** Terrariumtribe.com. *Houseplant Statistics 2026.* March 2026.
*Tier: T4. Pillar: I.*
Consumer-facing compilation of market data, Google Trends analysis 2020–2026, and industry statistics. Confirms post-pandemic normalisation with sustained elevated baseline. January 2026 shows a notable search interest spike. *C1: Garden centres retain 50.1% of total indoor plant market share in 2025 by value. C2: Google search interest in "houseplants" peaked in 2020–2021 and has normalised at levels above pre-pandemic baseline. I1: The market is not declining; it is stable at a structurally higher level than pre-2020.*

**I04.** RevenueCat. *State of Subscription Apps.* Annual report (most recent edition).
*Tier: T3. Pillar: I.*
Annual report on subscription app economics across all categories. Provides median conversion rates, LTV by category, and annual vs monthly pricing analysis. *C1: Annual subscription plans produce approximately 3x higher LTV than monthly plans in lifestyle app category due to lower annual churn. C2: Day-1 trial conversion median: 1.5–3% for lifestyle apps. I1: Default the subscription upsell to annual pricing; offer monthly as a secondary option.*

---

## J — Adjacent Topics (Tier T1–T5)

**J01.** Cummings, B.E. & Waring, M.S. "Potted plants do not improve indoor air quality: a review and analysis of reported VOC removal efficiencies." *Journal of Exposure Science & Environmental Epidemiology* 30 (2020): 253–261.
*Tier: T1. Pillar: J.*
Meta-analysis of plant VOC removal studies. Concludes that air exchange rates in real buildings would require 10–1,000 plants/m2 to achieve effects observed in sealed-chamber experiments. Directly contradicts popular NASA Clean Air Study misrepresentations. *C1: At typical building air exchange rates, the VOC removal contribution of houseplants is negligible compared to passive ventilation. C2: The NASA study's chamber conditions are not representative of any real indoor environment. I1: The app must not claim or imply air purification benefits from houseplants without the dose-response qualification.*

**J02.** Fogg, B.J. *Tiny Habits: The Small Changes that Change Everything.* Virgin Books, 2020.
*Tier: T2. Pillar: J.*
Fogg Behavior Model applied to habit formation. Relevant for watering reminder design and care routine building. *C1: Habit formation requires a trigger, motivation, and ability — care reminder systems fail when they deliver the trigger without ensuring motivation and ability are present. C2: Over-frequent reminders undermine motivation by creating anxiety. I1: Design watering reminders at the minimum frequency (archetype-driven) with easy snooze; never push more than once per scheduled interval.*

**J03.** Lee, M.S., Park, B.J., Lee, J., and Park, B.J. "Interaction with indoor plants may reduce psychological and physiological stress by suppressing autonomic nervous system activity in young adults: a randomized crossover study." *Journal of Physiological Anthropology* 34.1 (2015): 21.
*Tier: T1. Pillar: J.*
Well-cited randomised crossover study showing physiological stress reduction (skin conductance, heart rate) in a laboratory setting from interaction with real plants vs a computer task. *C1: Interaction with real plants significantly reduces sympathetic nervous system activation vs computer tasks in a laboratory setting. C2: Effect size is moderate; it does not establish clinical therapeutic benefit. I1: App copy can reference "research shows plant care supports wellbeing" — it is accurate at this level of specificity; more specific health claims are not warranted.*

**J04.** Wolverton, B.C., Johnson, A., and Bounds, K. "Interior Landscape Plants for Indoor Air Pollution Abatement." NASA Technical Report, 1989.
*Tier: T4 (historical). Pillar: J.*
The original NASA Clean Air Study. Plants tested in sealed chambers achieved meaningful VOC reduction under those conditions. The limitations are the sealed chamber methodology and dose-response problem documented in J01. *C1: The study tested a range of common houseplants in sealed 0.07m3 chambers — not representative of normal room conditions. I1: Do not cite this as evidence for air purification in product copy without the J01 qualification. The study is frequently misrepresented in plant marketing; being accurate about its limitations is a product differentiator.*

---

*End of Annotated Source List*
*Full Zotero library should contain minimum 300 entries. The above represents 40 curated seed entries across all pillars. Expand via forward/backward snowballing from each anchor source.*

---

## Working Glossary

**Air-filled porosity (AFP):** Fraction of substrate volume occupied by air after free drainage in a container, typically measured at -1 kPa matric potential. Primary predictor of root-zone oxygen availability.

**APG IV:** Angiosperm Phylogeny Group classification, fourth revision (2016). Current consensus molecular phylogeny of flowering plants; responsible for Sansevieria → Dracaena synonymy and other name changes.

**Aroid:** Member of family Araceae. Includes many top retail houseplants: Monstera, Philodendron, Pothos, Anthurium, Spathiphyllum, Aglaonema, Alocasia, Zamioculcas.

**Archetype:** Named substrate recipe class (Standard Houseplant, Aroid Chunky, Succulent Gritty, etc.). The primary output of the recommendation engine.

**CAM photosynthesis:** Crassulacean Acid Metabolism — water-conserving pathway in succulents and many epiphytes. CAM plants tolerate (and require) longer dry intervals between waterings.

**Calibration / ECE:** Expected Calibration Error measures how well a model's reported confidence matches its empirical accuracy. Target ECE < 0.05 for consumer-facing confidence display.

**Container capacity:** Water content of a substrate after free drainage in a container at its field capacity. Container-geometry dependent.

**Cultivar (cv.):** A cultivated variety, typically a named selection with specific horticultural traits. E.g., Monstera deliciosa 'Thai Constellation'. Distinct from a botanical variety (var.).

**Epiphyte:** Plant that grows on another plant for physical support, deriving water and nutrients from rain, air, and debris, not from soil. Many orchids and some aroids are epiphytic in nature.

**Hemi-epiphyte:** Plant that begins life as an epiphyte and later roots in soil, or vice versa. Monstera, Philodendron, and Epipremnum are hemi-epiphytes; this explains why they tolerate both chunky bark-based mixes and standard potting soil.

**FGVC:** Fine-Grained Visual Classification. CV task where the challenge is distinguishing between visually similar classes within a superclass (e.g., plant species within a genus).

**LECA:** Lightweight Expanded Clay Aggregate. Used as the substrate in semi-hydroponic ("semi-hydro") systems.

**LiteRT:** Google's rebranding (2024) of TensorFlow Lite. The primary on-device ML inference framework for Android.

**Open-set recognition:** The ability of a classifier to detect that an input does not belong to any of its known classes. Critical for handling photos of non-plant objects.

**Perched water table / hanging water column:** The zone of saturated substrate that persists at the bottom of a container regardless of drainage holes, due to capillary tension. Gravel or crocks at the pot bottom raise, not lower, this zone.

**Pl@ntNet:** French academic citizen-science plant ID system (CNRS/INRAE/Cirad/IRD). The primary deployed reference system for field flora.

**PlantCLEF:** Annual plant identification challenge within the LifeCLEF/CLEF evaluation forum. The field's principal benchmark.

**POWO:** Plants of the World Online. Kew's authoritative taxonomic resource. The product database canonical authority.

**Substrate archetype:** See Archetype above.

**Temperature scaling:** Post-hoc calibration method that divides model logits by a learned scalar T before softmax, reducing ECE with minimal impact on top-1 accuracy.

**Velamen:** Spongy outer tissue on the aerial roots of orchids. Absorbs atmospheric moisture. The reason orchid roots must not be buried in dense media.

---

*Document ends. Version 1.0. Date: 11 May 2026.*
*This document is the charter product of the literature review team and should be read in conjunction with the Zotero library, concept map, and risk register.*
