# Research Brief — Houseplant Soil & Potting Guidance (Android Application)

*A profound, exhaustive scoping document for the literature review team.*

**Project:** Houseplant Identification & Substrate Recommendation Engine
**Document version:** 1.0
**Date:** 11 May 2026
**Prepared for:** Literature Review and Synthesis Team
**Classification:** Internal — share within team and named collaborators only

---

## 0. How to use this brief

This document is the charter for a literature review effort that will inform the design and implementation of an Android application providing two core capabilities: visual identification of common houseplants and recommendation of an appropriate potting mix for each identified plant. The application is conceptually simple. The knowledge required to do it well is not. A correct recommendation depends on accurate identification, which depends on photographic conditions, model capability, and taxonomic disambiguation. A correct identification is useless if the recommended substrate would kill the plant in three weeks. Both halves of the product sit on top of dense, heterogeneous, multi-disciplinary literature: horticultural science, root-zone physics, soilless media chemistry, fine-grained visual classification, on-device machine learning, mobile UX, and the legal and ethical questions raised by giving care advice for living things that people love.

The team reading this brief is being asked to study that literature wide and deep — wider than feels reasonable on a first pass, deep enough that the engineering team can later make implementation decisions with confidence rather than guesswork. The structure of this document reflects that posture. Section 1 frames what we are actually building and why precision in scoping matters. Section 2 sets the research methodology so that everyone is grading sources on the same scale and producing artifacts that can be combined later. Sections 3 through 12 present each domain pillar in turn, with background, key questions, source pointers, and traps to avoid. Sections 13 through 15 cover deliverables, scheduling, and acceptance criteria. The appendices contain templates, glossaries, and a starter bibliography.

Read the whole document end-to-end once before starting on any pillar. The pillars are not independent. A decision about which plant identification dataset to fine-tune on has downstream consequences for which species the recommendation engine can support, which has consequences for which potting-mix templates need to be specified, which has consequences for the substrate ingredients we have to teach users to source. The reverse pressure is also real: if the literature shows that certain common houseplant genera have substrate requirements so specific that getting them wrong is harmful, we must be confident that the identifier can disambiguate at the relevant rank — sometimes species, sometimes cultivar.

When in doubt, prefer thoroughness to brevity. The cost of a missed reference at this stage is far greater than the cost of one extra week of reading.

---

## 1. Project context and vision

### 1.1 The product, stated plainly

The Houseplant Soil Potting Guidance application is an Android app whose user takes one or more photographs of a houseplant they own (or are considering buying or repotting), and receives in return: (a) an identification of the plant with a stated confidence level, with the option to refine if the model is uncertain between candidates; and (b) a recommendation for an appropriate potting mix, expressed both as a named template and as a recipe of ingredients with proportions, accompanied by enough explanation that the user understands why this mix suits this plant. The application should be usable by complete beginners — people who bought a Monstera deliciosa at a hardware store and discovered the included pot has no drainage — and should remain useful to enthusiasts who own twenty plants and want a reliable second opinion before repotting. It is a houseplant question, not a horticulture-industry question, and the design must respect that.

### 1.2 Why the literature matters here, more than usual

Many consumer apps can ship a thin recommendation engine on top of plausible-sounding heuristics and survive on UX polish. This one cannot, for three reasons. First, plant care advice has real-world stakes: a *Phalaenopsis* orchid potted in moisture-retentive peat-heavy mix will rot within a few weeks; a cactus in a dense compost-based mix may die before the user notices. Bad advice has visible, costly, distressing consequences. Second, the houseplant trade ships plants with retail labels ("Tropical Plant — Direct Sunlight Avoided") that are wrong or misleading at a rate that surprises non-specialists; we are not competing with already-good information, we are competing with confusion, and the bar is to be correct, not to be average. Third, the body of literature that would let us be correct exists, but is fragmented across academic horticulture, industry growers' guides, hobbyist communities of remarkable depth (notably aroid, succulent, orchid and carnivorous-plant societies), and a handful of recent machine learning papers on plant identification. Nobody has previously synthesized these specifically for a consumer Android product. We are the first team to do so, and that is the central deliverable of this research phase.

### 1.3 Working hypotheses to test through the literature

The literature review is not a neutral cataloguing exercise. It must adjudicate the following working hypotheses. Treat each as falsifiable; assemble the evidence.

1. **H1.** A small number of houseplant taxa — fewer than 200 species across roughly 40 genera — covers the overwhelming majority of plants owned by ordinary consumers worldwide. If true, this dramatically simplifies both the identification model and the recommendation template library.
2. **H2.** Potting-mix requirements cluster into a small, finite set of substrate archetypes (e.g., standard tropical, aroid chunky, succulent gritty, semi-hydro inert, ericaceous acid, epiphytic bark) such that almost every common houseplant maps to one archetype or a defined blend of two. If true, we can ship a recipe library rather than a generative model.
3. **H3.** On-device computer vision is sufficient in 2026 to classify the most common houseplants from a casual smartphone photo at top-1 accuracy meaningful for product use (>85% on the head of the distribution; useful uncertainty for the tail). If false, we need a cloud component, with all that implies for privacy, latency, and cost.
4. **H4.** Users will photograph plants in conditions substantially worse than research dataset images (poor light, cluttered backgrounds, occluded leaves, juvenile or stressed specimens, partial frames). The literature on robustness and domain shift in plant ID is large enough to guide our data-collection and augmentation strategy.
5. **H5.** The most common failure mode is not misidentifying a species but failing to distinguish a cultivar or variant whose substrate needs differ materially (variegated vs. non-variegated, dwarf vs. standard, terrestrial vs. epiphytic form of the same genus). The literature should tell us which disambiguations matter for care outcomes.
6. **H6.** Liability and ethical risk from misidentification is non-trivial and can be mitigated by careful UX, but cannot be eliminated by disclaimers alone. We need to understand the legal landscape before, not after, launch.

### 1.4 Out of scope, deliberately

To keep the literature review tractable, the following are explicitly out of scope at this stage. Surface them in your reading if they seem critical, but do not pursue them: outdoor gardening, food crops, agricultural production at any scale, hydroponic systems for vegetables, bonsai (specialized in ways that warrant its own product), terrariums and paludariums (different substrate physics), plant pest and disease diagnosis (large adjacent product), nutrient-deficiency diagnosis from leaf imagery, and watering-schedule recommendations (interacts with substrate but is a separate product feature).

---

## 2. Research methodology

### 2.1 The shape of a profound literature review

We adopt a modified scoping-review methodology, drawing from the Arksey & O'Malley framework as updated by Levac et al. and from the PRISMA-ScR (Scoping Review) reporting standard. A scoping rather than a systematic review is correct here because our question is interdisciplinary and the goal is to map and synthesize a heterogeneous body of work, not to compute a meta-analytic effect size. Within each pillar, however, where a narrow empirical question can be answered (for example, what is the typical air-filled porosity of a commercial peat-perlite mix versus a coco coir-pumice mix), apply a small systematic search and document it as such.

### 2.2 Source quality tiers

Every source you read should be tagged with a quality tier so that downstream readers can quickly understand the weight of evidence behind any claim. Use this rubric:

| Tier | Type of source | Examples |
| --- | --- | --- |
| T1 | Peer-reviewed primary research | *HortScience*, *Scientia Horticulturae*, *Acta Horticulturae*, IEEE/CVF and ICCV/CVPR papers on plant ID, journals of the American Society for Horticultural Science. |
| T2 | Peer-reviewed or authoritative reviews and reference works | RHS Encyclopedia of Plants & Flowers, Kew's Plants of the World Online, *Soilless Culture: Theory and Practice* (Raviv & Lieth), Manual of Houseplants (RHS). |
| T3 | Industry technical literature | Premier Tech / PRO-MIX growers guides, RHP substrate certification standards, Berger Substrate technical notes, manufacturer agronomy bulletins. |
| T4 | Reputable horticultural extension and government | Cooperative Extension services (e.g., University of Florida IFAS, Missouri Botanical Garden Plant Finder, RBG Kew), USDA, EU CORDIS reports. |
| T5 | Specialist society and hobbyist long-form sources | International Aroid Society, American Orchid Society, Cactus and Succulent Society of America, International Carnivorous Plant Society — frequently the only deep source on cultivation specifics. |
| T6 | General web content, blogs, retailer guides | Use sparingly. Useful for what the user community believes (which the product must understand) but not for substantive claims. |
| T7 | Generative-AI-authored or anonymous synthesis | Do not cite. Use only as a discovery aid pointing to T1–T5 primary sources. |

### 2.3 Search strategy

For each pillar, run searches in at least three independent indexes: a generalist scholarly index (Google Scholar or Semantic Scholar), a discipline-specific one (Web of Science, Scopus, AGRICOLA for horticulture; arXiv, OpenReview, IEEE Xplore, ACM DL for ML; CORE or BASE for grey literature), and a specialist database where applicable (CABI for plant sciences, GBIF for occurrence and taxonomy). Record every query verbatim, the database, the date, and the number of hits before filtering. This is not bureaucracy; it makes the review reproducible and prevents the team from believing it has searched exhaustively when it has in fact only searched comfortably.

Use forward and backward snowballing aggressively. A single well-cited 2019 paper on potting-substrate physical properties will lead, in two snowball iterations, to most of the substantive literature on the topic. The same is true for plant identification: the Pl@ntNet papers and PlantCLEF challenge papers are the spine of the field; everything else hangs off those vertebrae.

### 2.4 Note-taking and synthesis

Every source enters Zotero (or an equivalent reference manager the team agrees on) with full bibliographic metadata, PDF attached where possible, a tier tag, a pillar tag, a one-paragraph annotation in the reader's own words, and three to five "extracted claims" — discrete factual or methodological statements that we may want to surface in the final synthesis. Do not extract claims you do not understand well enough to defend. The annotation template appears in Appendix B.

Synthesis happens in two passes. The first pass is pillar-internal: each pillar lead writes a 5–10 page synthesis memo summarizing the state of evidence, open questions, and implementation implications. The second pass is integrative: the full team meets and integrates across pillars, producing a single Implementation Implications Memo that maps research findings to design and engineering choices. Plan for at least two integration sessions; the cross-pillar interactions are where the value of doing this exhaustively actually appears.

### 2.5 Living document discipline

The literature does not stop being published when the review ends. Designate a section of the final synthesis as living: each team member commits to a quarterly check on the three or four most active queries in their pillar for the first year post-launch. Plant identification research moves fast; substrate science moves slowly but with occasional landmark papers; mobile ML tooling and policy churn constantly.

---

## 3. Pillar A — Houseplant botany, taxonomy, and care

### 3.1 Why a software team must learn botany

It is not optional. The recommendation engine reasons about plants; if it reasons about them at the wrong granularity, it will give wrong answers. Three concrete examples make the point. (a) *Ficus lyrata* (fiddle-leaf fig) and *Ficus elastica* (rubber plant) are in the same genus but tolerate quite different substrate moisture; treating them identically because the model is satisfied at genus rank will produce a noticeably worse recommendation for *lyrata*. (b) Many "Philodendrons" sold at retail are *Monstera* or *Epipremnum*, and the differences in suitable mix between epiphytic aroids and terrestrial philodendrons are real. (c) *Sansevieria* has been formally moved into *Dracaena*; the literature is split between names, retailers use both, and the user types whatever is on the plant tag — the system must be robust to this synonymy.

### 3.2 Topics to cover

- Linnaean nomenclature and the conventions of the International Code of Nomenclature for algae, fungi, and plants (ICN); how species, subspecies, varieties (var.), forms (f.), and cultivars (cv.) are designated and why this matters for our database keys.
- The APG IV system of angiosperm classification and the consequences for genera that have recently moved (*Sansevieria* → *Dracaena*; some *Aglaonema* reassignments; *Zamioculcas* remains in Araceae).
- Ecological strategies relevant to substrate: terrestrial (most *Ficus*, *Calathea*, *Maranta*, most *Begonia*), epiphytic (most orchids, many aroids in nature, *Tillandsia*), hemi-epiphytic (*Monstera*, *Philodendron*, *Pothos* in juvenile vs. mature phases), lithophytic (some *Hoya*, *Sansevieria*-now-*Dracaena*), succulent (Crassulaceae, Cactaceae, many *Euphorbia*).
- Root architecture: thick fleshy aerial roots of climbing aroids, hair-like fibrous roots of ferns, water-storing tuberous roots of ZZ plant (*Zamioculcas*), velamen-coated aerial roots of orchids — and the substrate physics that suit each.
- Plant water relations: transpiration, CAM photosynthesis in succulents and many epiphytes, why CAM plants tolerate (and require) longer dry intervals.
- Common-name confusion: pothos vs. devil's ivy vs. *Epipremnum aureum*; jade plant vs. money tree vs. lucky bamboo; ZZ plant nomenclature in retail.
- Trade-level taxonomy: which retail tags map to which scientific names; how big-box retailer tagging differs from specialty nursery tagging.
- Pet and child toxicity: ASPCA toxic and non-toxic plant list, Plants Poisonous to People (UF/IFAS), Royal Horticultural Society *Potentially Harmful Garden Plants* — relevant both for safety surfacing in the app and for legal exposure.

### 3.3 Anchor sources

- Plants of the World Online (Kew) — the authoritative taxonomy and synonymy resource. https://powo.science.kew.org
- Royal Horticultural Society *Gardeners' Encyclopedia of Plants and Flowers* — the practical reference closest to a houseplant care canon.
- Hessayon, D.G. — *The House Plant Expert* series. Dated in tone, still useful for the canon of common species and their needs.
- Croat, T.B. — published work on Araceae taxonomy, including *A Revision of Anthurium* and *Philodendron* monographs.
- International Aroid Society Bulletin (*Aroideana*) — the deepest single source on aroid identification and culture.
- USDA PLANTS Database, Global Biodiversity Information Facility (GBIF), Catalogue of Life — for taxonomy crosswalks and synonym resolution.
- Royal Horticultural Society Plant Finder — for cultivar-level information.
- Missouri Botanical Garden Plant Finder — pragmatic per-species care information.

### 3.4 Key research questions

1. Which species, in which proportions, dominate the global houseplant retail market? Map regional differences (North America vs. Western Europe vs. Australia vs. East Asia). The *Floraculture International* market reports and the AIPH Statistical Yearbook are starting points.
2. For each of the top 100 species by retail volume, what is the canonical taxonomic name, the top three retail synonyms, and the most common name-confusion failure modes?
3. Where in the taxonomy is the right granularity for care recommendations? Genus is too coarse for some lineages, species is right for most, cultivar matters for a small but commercially important set (variegated *Monstera*, Pink Princess *Philodendron*, *Hoya* cultivars).
4. Which species or genera in our top-100 are CITES-listed or otherwise legally restricted in the user's likely jurisdictions? (Notable: many succulent *Euphorbia*, some orchids, many cycads.)

### 3.5 Traps to avoid

Older horticultural references are tonally confident and taxonomically out of date. Always cross-check names against POWO. Pre-2017 sources will still call ZZ plant *Zamioculcas zamiifolia* (correct), will still call snake plant *Sansevieria trifasciata* (now *Dracaena trifasciata*), and will still split-and-lump in ways that don't reflect current consensus. Treat older references as good for cultural advice and worse for naming.

---

## 4. Pillar B — Substrate science (growing media)

### 4.1 The discipline you are entering

Soilless growing media is a mature applied science with about sixty years of literature. The foundational textbook is *Soilless Culture: Theory and Practice* (Raviv & Lieth, 2nd ed., 2019); it should sit on every team member's desk physically, not just digitally. The discipline understands the physical, chemical, and biological behaviour of growing media in terms that map cleanly to product decisions: a recommendation engine that outputs a recipe needs to know what each ingredient does, why it does it, and what the substitutes are.

### 4.2 The physical properties that matter

Substrate behaviour is dominated by physics, especially in containers, where the relationship between container height and water-holding behaviour produces non-intuitive outcomes. The team must internalize these properties.

- Total porosity, container capacity, and air-filled porosity (AFP). AFP after drainage is the single most informative property for predicting root health. Standard methods: De Boodt and Verdonck (1972); ISHS standard methods later codified by Bures and others.
- Water release curves and the concept of "easily available water" between -1 kPa and -5 kPa matric potential. The width of this band is what makes coir-based mixes feel forgiving and pure-bark mixes feel demanding.
- Bulk density, particle size distribution, and the geometry of pore space — why mixing two well-drained components doesn't always produce a well-drained mix (the small particles fill the gaps).
- Hydraulic conductivity, capillary rise, and the perched water table phenomenon in shallow pots — the reason "crocks at the bottom" advice is wrong and persists anyway.
- Cation exchange capacity (CEC), pH buffering, and electrical conductivity (EC) — chemistry the user never sees but that determines whether their fertilizer regime works.
- Wettability and rewetting behaviour, especially of peat-based and coir-based media that go hydrophobic when fully dry. The literature on wetting agents (surfactants) is small but useful.

### 4.3 The ingredient catalogue

Each ingredient warrants its own short literature dive. Cover at minimum:

- **Sphagnum peat moss** — origin, processing grades, hydraulic and chemical properties, environmental controversy (peat bog ecology), regulatory pressures in the EU and UK.
- **Coco coir / coco peat** — production, EC/salt-flushing requirements, sodium and potassium content, lifecycle comparisons with peat.
- **Perlite** — mineralogy, dust hazards, particle grades (coarse, medium, fine, super-coarse), substitutes.
- **Pumice** — physical properties, sourcing, why aroid hobbyists prefer it to perlite for chunky mixes.
- **Vermiculite** — exfoliated mica, CEC, water-holding, contamination concerns (historical asbestos in Libby vermiculite).
- **Pine bark, fir bark, orchiata** — particle size grading, decomposition rate, nitrogen draw-down effects.
- **Sphagnum moss (live and long-fibered)** — distinct from peat; used for orchids and aroid propagation.
- **LECA (lightweight expanded clay aggregate)** — semi-hydro systems, water-wicking, the recent enthusiast literature on passive hydroponics for aroids.
- **Akadama, kanuma, kiryu** — Japanese fired-clay substrates used in bonsai and succulent culture; particle stability, water curve.
- **Activated charcoal** — historical use in terraria; weak evidence base for the claims often made for it.
- **Sand and grit** — particle size matters enormously; coarse silica sand differs from beach sand.
- **Worm castings, biochar, mycorrhizal inoculants** — biological additives, evidence base ranges from strong (worm castings improve seedling growth in greenhouse trials) to weak (mycorrhizal inoculant efficacy in already-fertilized container culture).
- **Fertilizer and slow-release components** — Osmocote and equivalents, time-release nutrient curves, NPK ratios appropriate to indoor light levels.

### 4.4 The substrate archetypes we expect to recommend

The literature, especially the practical horticultural and hobbyist literature, has converged on a small number of working substrate archetypes for indoor plants. Map the literature evidence behind each. The list below is provisional; refine it through reading.

| Archetype | Typical composition | Target group |
| --- | --- | --- |
| Standard houseplant | Peat or coir 60%, perlite 30%, bark fines 10%, lime, slow-release fertilizer | General foliage plants (*Ficus*, *Dracaena*, *Spathiphyllum*, *Chlorophytum*) |
| Aroid chunky | Pine/orchid bark 40%, coco coir 25%, perlite or pumice 20%, sphagnum 10%, charcoal 5% | Climbing aroids: *Monstera*, *Philodendron*, *Epipremnum* |
| Succulent gritty | Pumice or akadama 40%, coarse sand or perlite 30%, coir or pine fines 30%, low fertility | Crassulaceae, most cacti, terrestrial *Sansevieria* / *Dracaena trifasciata* |
| Cactus pure mineral | Pumice 50%, akadama or lava 30%, coarse sand 20%, almost no organic | Demanding cacti, *Lithops*, *Conophytum* |
| Epiphytic orchid (bark) | Coarse fir or orchiata bark 80%, charcoal 10%, perlite 10% | *Phalaenopsis*, *Cattleya*, most *Dendrobium* |
| Semi-hydro inert | LECA 100% with hydroponic fertilizer | Many aroids, *Hoya*, *Sansevieria*; increasingly popular |
| Acidic / ericaceous | Coir-peat blend with low pH (4.5–5.5), added pine bark | *Camellia*, *Gardenia*, some azaleas indoors |
| Moisture-retentive | Peat or coir 70%, sphagnum 15%, fine bark 15%, perlite minimal | *Calathea*, *Maranta*, ferns, *Begonia* (some) |
| Carnivorous | Sphagnum peat or long-fibered sphagnum + perlite, low mineral, low fertility, distilled water | *Drosera*, *Sarracenia*, *Nepenthes*, *Dionaea* |

### 4.5 Sustainability

Peat is the largest single ingredient in commercial houseplant substrates worldwide and the focus of growing regulatory pressure. The UK has legislated phase-outs of peat in retail bagged growing media on a published schedule; the EU horticulture sector is responding similarly. The team must understand the alternatives literature (coir lifecycle assessments are nuanced — shipping coir from South Asia to Europe has its own carbon cost), industry's response, and the implications for what we should recommend users buy. A recommendation engine that points users at peat-heavy mixes will look dated within the product's first few years.

### 4.6 Anchor sources

- Raviv, M. & Lieth, J.H. (eds). *Soilless Culture: Theory and Practice*, 2nd ed., Elsevier, 2019. The textbook.
- De Boodt, M. & Verdonck, O. *The Physical Properties of the Substrates in Horticulture*. *Acta Horticulturae* 26, 1972. Foundational; still cited.
- Bures, S. — series of papers on substrate physical characterization methods.
- Bilderback, T.E. et al. — work on container substrates at NCSU.
- Argo, W.R. & Fisher, P.R. — *Understanding pH Management for Container-Grown Crops*. Useful industry/academic crossover.
- Premier Tech (PRO-MIX) Grower Services Technical Information — industry but technically careful.
- RHP (Regeling Handels Potgronden) certification — Dutch substrate quality standards, the industry reference.
- PAS 100 (BSI), AS 3743 (Australia) — national substrate standards.
- *Acta Horticulturae* proceedings of the International Symposia on Growing Media — every two to three years, the field's set-piece event.
- International Aroid Society and r/houseplants substrate threads — high-signal hobbyist work; tier T5 but often empirical and tested.

### 4.7 Key research questions

1. For each substrate archetype in §4.4, what is the typical range of AFP, container capacity, and pH actually achieved by the recipe? Provide ranges, not point values.
2. What is the regional availability of each ingredient? A recipe specifying orchiata bark is useless to a user in a country where it is not sold.
3. What is the evidence base for adding charcoal, mycorrhizae, worm castings, or biochar to standard houseplant mixes? Where is the evidence strong, where is it weak?
4. What does the peat-alternative transition imply for our recipe library over a five-year horizon?
5. What substrates do commercial growers actually use to grow the plants that end up in retail, and how does shipping-mix differ from a healthy long-term home mix? The literature on "shipped in a too-wet mix" is mostly tacit knowledge in the trade.

---

## 5. Pillar C — Plant identification via computer vision

### 5.1 The shape of the field

Plant identification from images is a mature subfield of fine-grained visual classification (FGVC). It has a long-running annual challenge (PlantCLEF, part of LifeCLEF inside the broader CLEF evaluation forum), a flagship deployed system (Pl@ntNet, with around a hundred million observations by 2024 and a publicly described model), several open datasets (PlantNet-300K, iNaturalist's plant subset, the original Oxford 102 Flowers, and many region-specific sets), and an active community of academic and industrial researchers. The product team will not be training a model from scratch and need not. The team will, however, need to decide: which open foundation model or open weights starting point; which dataset(s) to fine-tune on; what data we collect from our own users and how; and what shape the inference pipeline takes (on-device, cloud, or hybrid).

### 5.2 What the literature must answer

- How well do current state-of-the-art models perform on common houseplants specifically, as distinct from the broad-spectrum flora those models are usually evaluated on? The PlantCLEF tasks generally tilt toward field flora; our distribution is different and indoor lighting is different.
- Which backbone families (Convolutional Networks, Vision Transformers, hybrid architectures, CLIP/SigLIP-style multimodal) currently lead on FGVC benchmarks and which lead specifically on plant data?
- What is the state of on-device inference in 2026? MobileNetV3/V4, EfficientNet-Lite, EfficientFormer, MobileViT, FastViT, the various distilled ViT variants; quantization schemes (post-training int8, QAT, mixed precision); pruning.
- What are the published numbers for confidence calibration on FGVC tasks? Most plant ID papers report top-1 and top-5 accuracy; far fewer report Expected Calibration Error, which is what we actually need.
- How is the field handling open-set recognition (deciding that the photo is not in the model's known classes)? Especially relevant because users will photograph their living-room rug as often as their dracaena.
- Self-supervised pretraining for plants (DINO, MAE, contrastive methods on plant data) and how much it helps when labelled data is biased.
- Multimodal grounding: can a CLIP-style model that has read horticultural text outperform a vision-only classifier of the same compute budget, especially for rare species?

### 5.3 Datasets to evaluate

- PlantNet-300K — about 306k images, 1081 species, biased toward European flora, but well-curated and openly licensed.
- Pl@ntNet API and observation database — much larger, partially CC-licensed depending on observer choices; check terms.
- iNaturalist plant subset — millions of images, research-grade and casual grades. Strong on temperate flora, less strong on tropical houseplants.
- PlantCLEF challenge releases — yearly datasets, varying focus.
- Oxford 102 Flowers — small, classic; mostly useful for sanity-check training runs.
- Plant Pathology challenges (Kaggle) — disease focus, not identification, but a source of intra-species variability.
- Specialty datasets: aroid identification datasets emerging in 2024–2025 from the enthusiast-academic interface; succulent and cactus image sets; the GBIF media corpus indexed by species.
- Pinterest, Reddit, plant retailer image scrapes — legally fraught, but the trade and enthusiast communities have made enormous photo archives that mirror the deployment distribution far better than research datasets do.

### 5.4 Anchor sources

- Goëau, H., Bonnet, P. & Joly, A. — Pl@ntNet and the LifeCLEF / PlantCLEF papers. Run a back-citation pass from the most recent PlantCLEF overview paper; this captures the field's central thread.
- Cole, E. et al. — *Are Fine-Grained Recognition Datasets Reaching Saturation?* CVPR 2022.
- Van Horn, G. et al. — iNaturalist papers and challenges.
- Singh, A., Jones, S. et al. — survey papers on plant identification with deep learning, e.g., the regular *Computers and Electronics in Agriculture* reviews.
- Howard, A. et al. — MobileNet papers, V1 through V4.
- Vasu, P.K.A. et al. — MobileOne (Apple), FastViT papers.
- Mehta, S. & Rastegari, M. — MobileViT family.
- On-device frameworks: TensorFlow Lite documentation (now part of LiteRT), MediaPipe model cards, Google ML Kit, ONNX Runtime Mobile.
- Calibration: Guo, C. et al. (2017) *On Calibration of Modern Neural Networks*; and the more recent literature on calibration for fine-grained tasks.
- Open-set recognition: Geng, C. et al. — *Recent Advances in Open Set Recognition: A Survey*, IEEE TPAMI 2021.
- Bonnet, P., Joly, A. et al. — papers on Pl@ntNet active learning and user-corrected labels at scale.

### 5.5 Key research questions

1. Which architecture and weight starting point gives the best accuracy / latency / app-size tradeoff on a target benchmark we define ourselves (top-200 retail houseplants, photographed under realistic conditions)?
2. How much of the work can be done on-device on a mid-range Android phone (4–6 GB RAM, mid-tier SoC) at sub-second latency, and what fraction of queries must fall back to cloud for accuracy or for rare species?
3. What is the lowest-cost path to producing 5k–20k product-realistic images per top species — synthetic data, augmentation strategies, controlled crowdsourcing, partnerships with nurseries, internal photography?
4. What does responsible release look like for a consumer plant identifier? The literature on disclosure of model limitations and confidence presentation is small and worth synthesizing.

---

## 6. Pillar D — Android engineering, on-device ML, and platform constraints

### 6.1 Modern Android in 2026

Android development has consolidated decisively on Kotlin, Coroutines and Flow for asynchrony, and Jetpack Compose for UI. The team should be reading not just framework documentation but the more thoughtful architectural literature: Google's official Architecture Guidance, the various "Now in Android" reference codebases, and the small but high-quality body of writing on Compose performance. The target API level for launch will be Android 14 or 15 (API 34/35); minimum supported API is a product decision that should consider the long tail of mid-range Android devices in our likely launch markets.

### 6.2 Topics to cover

- Kotlin idioms; structured concurrency with coroutines; Flow vs. SharedFlow vs. StateFlow.
- Architecture patterns: MVVM, MVI, and the unidirectional data flow consensus; the Compose state-hoisting model and its implications.
- Jetpack libraries the project will use: Compose (UI), Navigation, CameraX (camera capture), Room (local DB for plant records), DataStore (preferences), WorkManager (background sync), Hilt (DI).
- Image capture and preprocessing pipelines: CameraX use cases, ImageProxy formats (YUV_420_888 vs. RGBA), efficient bitmap handling, avoiding `OutOfMemoryError` on flagship cameras.
- On-device inference frameworks: TensorFlow Lite / LiteRT, ONNX Runtime Mobile, MediaPipe LLM and Vision tasks; their delegate ecosystems (NNAPI deprecation in API 35, GPU delegate, Hexagon DSP delegate on Qualcomm, NPU paths on Tensor / Dimensity / Exynos).
- Model packaging: shipping with the APK vs. Play Asset Delivery vs. on-demand download; baseline profile generation; app-size implications.
- Background work: WorkManager constraints, exact alarms post-Android 12 restrictions, foreground service types post-Android 14.
- Camera permission UX, location permission UX (do we want city-level location for climate context?), notification permission on Android 13+.
- Privacy: Data Safety form on Play, photo and video permission post-Android 14 Photo Picker, the new partial photo access flows.
- Form factors: foldables (Pixel Fold, Samsung Galaxy Z Fold), large screens (tablets, Chrome OS via Android emulation), and the Compose adaptive layouts story.
- Testing: instrumented tests, Compose UI tests, screenshot/snapshot testing for Compose, Robolectric, Firebase Test Lab matrices.
- Performance: app startup (cold/warm/hot), jank tracking, baseline profiles, Macrobenchmark, R8 optimization, dynamic feature modules.
- Crash/performance/analytics: Firebase Crashlytics, Performance Monitoring, and the alternatives (Sentry, Embrace) — the privacy story differs across them.
- Play Console policies relevant to a plant ID app: medical/health claims (we are not making medical claims, but plant safety claims are adjacent), AI-generated content disclosures, photo and video uploads, accuracy claims in marketing.

### 6.3 Anchor sources

- Android Developers documentation — developer.android.com. Specifically: Architecture Guide, Compose documentation, CameraX, ML on Android, App Quality / Core User Journeys.
- Now in Android sample app — Google's reference modern Android codebase. Read the actual code.
- Google I/O sessions on Compose performance, baseline profiles, on-device ML, AI on Android.
- Books: *Android UI Development with Jetpack Compose* (Schwarz), *Kotlin Coroutines: Deep Dive* (Marcin Moskała).
- Google Play policies center, Data Safety section guides, Permissions changelog by Android version.

### 6.4 Key research questions

1. What is the minimum SoC class we can support if we ship a 20–80 MB on-device model and target sub-second inference? Pixel 6a, Galaxy A-series, Redmi Note class. Below that we either degrade gracefully to cloud or block install.
2. What is the right packaging for the model — installed APK, Play Asset Delivery feature module, or on-demand cloud-first with offline fallback after first use?
3. How do we handle the photo permission flow respectfully on Android 14+ with the new partial Photo Picker, particularly for users who don't want to grant any media access and would prefer in-app camera only?
4. What is the appropriate analytics minimum: which events do we instrument from day one to know whether identifications are succeeding, without compromising user privacy?
5. Which Play Store category, content rating, and Data Safety disclosures apply to us? Pre-clearing Play policy before launch saves weeks.

---

## 7. Pillar E — UX, product design, and the competitive field

### 7.1 What's already out there

A serious literature review of the competitive field is non-negotiable. The category is crowded but uneven, and several incumbents have shipped questionable practices (overclaiming identification accuracy, paywalls dropped mid-flow, dark-pattern subscriptions). Knowing the field in detail tells us what the user expects, what they hate, and where the obvious product gaps are.

For each of the apps below, a member of the UX team should install it, complete the onboarding, perform at least ten identifications with varied plants, attempt a paywall scenario, and write a short structured review against a common rubric (camera UX, accuracy, recommendation specificity, monetization aggressiveness, privacy posture, plant-care content quality, app polish, accessibility).

- PictureThis (Glority) — market leader by installs; aggressive paywall, opaque accuracy.
- Pl@ntNet (CNRS / INRAE / Cirad / IRD) — research-backed, free, more accurate on European wild flora than on retail houseplants.
- Planta — care-tracking heavy; identification is secondary; Scandinavian design language.
- Greg — community-flavoured; care reminders centred.
- Blossom — similar feature set to PictureThis.
- Seek by iNaturalist — taxonomy-grade; not house-plant-specialized; good model for honest confidence presentation.
- Google Lens — free, broad, the implicit competitor for users who don't install anything.
- PlantIn, NatureID, AIPlantFinder — second tier, useful for understanding the floor of the category.
- Specialist apps: Aroid ID, Orchid ID variants — much smaller, often more accurate within scope.

### 7.2 UX literature to read

- Nielsen Norman Group — usability heuristics, mobile UX research.
- Google Material Design 3 / Material You — the platform-native design language we'll use unless we deliberately deviate.
- Camera UX literature: research on user-guided photo capture quality (computational photography papers from Google and Apple's CVPR / SIGGRAPH publications are oddly relevant).
- Don Norman — *The Design of Everyday Things*, *Living with Complexity*.
- Behavioural design: BJ Fogg — *Tiny Habits* and the Fogg Behavior Model; Nir Eyal — *Hooked* (read critically, especially around dark patterns we must avoid).
- Accessibility: WCAG 2.2 and 3.0 (draft), Android Accessibility Guide, TalkBack guidance.
- Confidence and uncertainty presentation in consumer AI products: a small but growing literature — papers from Microsoft Research (HAX Toolkit), Google PAIR Guidebook, and other work on calibrated confidence in user-facing AI.
- Onboarding research: Mobile Onboarding patterns (Appcues, UserOnboard teardowns), the academic literature on tutorial design in apps.

### 7.3 Specific UX questions to answer through the literature

1. How should we present model confidence? "94% sure this is a *Monstera deliciosa*" vs. "Most likely *Monstera deliciosa*, with *Monstera adansonii* a strong alternative" vs. visual treatments. The HCI literature is split; product testing will be required, but the literature gives us the menu.
2. Camera capture flow: live guidance ("move closer to the leaf") vs. single-shot vs. multi-shot (whole-plant, leaf-close, soil-line). Pl@ntNet's multi-shot taxonomy of organ types is informative; aroid hobbyists almost always need petiole and leaf back to disambiguate.
3. Recommendation presentation: archetype name + recipe table + buy-list vs. a single composed recipe vs. progressive disclosure. What does the user actually act on?
4. Onboarding for users with zero plants vs. users with many plants — the product needs both, and they want different first runs.
5. Monetization fairness: free identifications vs. paywall, where exactly the line falls in our category, and what kinds of monetization the user community visibly hates (see r/houseplants and App Store reviews for PictureThis and others).
6. Plant naming respectfulness: some common houseplant trade names are derived from problematic colonial-era branding or are culturally insensitive ("Wandering Jew" → now *Tradescantia zebrina*; "Mother-in-Law's Tongue" → snake plant or *Dracaena trifasciata*). The horticultural literature has been moving on this; we should follow the move.

---

## 8. Pillar F — Recommendation logic

### 8.1 The shape of the recommendation problem

Once a plant is identified, the recommendation problem is conceptually a function from (species or cultivar, optional user context) to (named substrate archetype, recipe, sourcing guidance, repotting advice). The literature relevant to this problem is partly horticultural (knowing what each plant wants), partly the recommender-systems literature (when and how to personalize), and partly the explanation-of-AI literature (because the user must trust the recommendation to act on it).

### 8.2 Rule-based vs. learned recommendation

For our domain, a rule-based recommender with a hand-curated knowledge base is almost certainly the right starting point, for four reasons: the cardinality is small (a few hundred taxa), the source-of-truth is human horticultural knowledge rather than user behaviour, errors are interpretable, and we cannot collect outcome data quickly enough to train a learned recommender well. The literature on hybrid recommenders is nevertheless useful, particularly around how to incorporate user feedback over time — "I followed this recommendation and my plant died/thrived" is a strong signal we should design for from day one.

### 8.3 Topics to cover

- Knowledge-base structure: how to model species, cultivars, growth phases, substrate archetypes, and ingredients in a way that supports later additions without schema rewrites. Ontology design literature (OWL, SKOS) is heavier than we need; lightweight typed records with controlled vocabularies are usually enough.
- Personalization signals: user climate (zone or latitude), home environment (light, humidity), how often the user reports under- or over-watering, prior plant fatalities. The literature on conversational care apps gives some templates.
- Explainability: what does the user need to read to trust a substrate recommendation? Probably (a) why this archetype, in one sentence; (b) the recipe; (c) what to do if an ingredient is unavailable. The Pl@ntNet and Seek confidence-presentation approaches are useful comparators.
- Edge cases: hybrids, cultivars, plants whose substrate needs change as they mature (*Monstera* juvenile vs. mature; many bromeliads), plants whose retail moisture-loving substrate is wrong long-term. The recommendation needs to know about these explicitly.
- Confidence propagation: when identification confidence is low, the recommendation must hedge. Either narrow to the safe intersection of plausible species or surface the ambiguity to the user.
- Recommendation evaluation: in the absence of long-term outcome data, evaluate against (a) horticultural expert review (a small panel scoring sampled recommendations), (b) consistency tests (does the system give the same recommendation to the same plant photographed twice differently?), (c) failure-mode probes (deliberately ambiguous queries).

### 8.4 Anchor sources

- Ricci, F., Rokach, L. & Shapira, B. (eds) — *Recommender Systems Handbook*, 3rd ed., Springer, 2022. The field overview.
- Aggarwal, C.C. — *Recommender Systems: The Textbook*, 2016.
- PAIR Guidebook (Google) — accessible HCI-flavoured guidance on AI in product.
- Microsoft HAX Toolkit — design patterns for human-AI interaction.
- The horticultural literature for each substrate archetype (Pillar B).

### 8.5 Key research questions

1. What is the right knowledge-base granularity? Per-species is workable for a few hundred entries; per-cultivar is necessary for the commercially important variegateds.
2. How do we update the knowledge base? Is it human-curated only, or do we accept community-suggested edits with review?
3. How should the recommendation behave when identification is uncertain between species with substantially different substrate needs?
4. What inputs from the user (climate, home environment) actually improve recommendation quality enough to be worth the friction of asking for them?

---

## 9. Pillar G — Data sources, licensing, and intellectual property

Every dataset, every image, every botanical fact in the product comes with a provenance and a license. Get this wrong, and a takedown letter from a botanical garden's legal department or a class-action over unlicensed image scraping will end the product. The good news is that the major source providers in this domain — Kew, GBIF, iNaturalist, Pl@ntNet — have well-published terms, and many of them encourage downstream use.

### 9.1 Topics to cover

- Creative Commons licensing nuances: BY, BY-SA, BY-NC, BY-ND. CC0 and public-domain mark. The difference matters: BY-NC excludes commercial use including ad-supported apps; BY-SA forces share-alike on derivative datasets.
- Pl@ntNet API terms of use and what "non-commercial research" means in 2026.
- iNaturalist licensing of media and observation data — heterogeneous, observer-set, must be respected per-record.
- GBIF data use agreement and the citation requirement.
- Kew's Plants of the World Online: scraping vs. licensed API access; data citation conventions.
- Database rights in the EU (sui generis database right, Directive 96/9/EC) — relevant if we want to ingest taxonomic data from European databases at scale.
- Scraping legality: the patchy state of US case law (hiQ v. LinkedIn, the Van Buren CFAA reading), EU posture, and the practical industry consensus.
- User-generated content (UGC) terms in our own ToS: what license we ask the user to grant us when they upload a plant photo, and how to balance that against user expectations.
- Model weights licensing: most of the open weights we'd consider (MobileNet variants, ViTs from Google, CLIP variants) ship under permissive licenses, but some are research-only. Check before fine-tuning.
- Plant variety rights (UPOV, US PVPA): some cultivars are protected — usually a commercial nursery concern, occasionally relevant to us if we recommend propagating.

### 9.2 Anchor sources

- CC licensing documentation — creativecommons.org/licenses.
- GBIF data use page and Best Practices for Citing Biodiversity Data.
- iNaturalist API documentation and licensing FAQ.
- Pl@ntNet terms of service.
- Recent academic and trade-press analyses of dataset licensing in ML (e.g., Data Provenance Initiative).

---

## 10. Pillar H — Privacy, ethics, and regulation

### 10.1 Privacy

The product is privacy-sensitive in three subtle ways. First, every uploaded photo may contain EXIF metadata including precise GPS coordinates; we must either strip metadata at upload or surface this clearly. Second, plant photos often inadvertently include parts of the user's home, occasionally including people, identifiable documents on a desk, screens with sensitive content. Third, the recommendation engine benefits from knowing climate / locale, which is a soft personal signal.

- GDPR (EU/EEA) and the UK GDPR — lawful bases for processing, the right to erasure, data minimization, data protection impact assessments.
- CCPA / CPRA (California) and the broader US state-level patchwork (Virginia, Colorado, Connecticut, Utah, more states adopting).
- Brazil LGPD, Japan APPI, South Korea PIPA — relevant for any launch outside North America.
- COPPA — Children's Online Privacy Protection Act in the US, and Google Play's Families policy. If we accept users under 13, the compliance burden is heavier.
- Google Play Data Safety form requirements and the specifics of "photos" disclosure.
- EXIF stripping standards and library options (libexif and the Android-native ExifInterface).

### 10.2 Ethics

- Misidentification harm: if the model identifies a toxic plant as a non-toxic look-alike, a pet or child could be hurt. The literature on AI accuracy disclosures and product safety for advisory AI (medical AI disclosure especially) is relevant by analogy.
- Endangered plant trade: CITES Appendix I and II species. Some succulents (notably *Conophytum*, *Tylecodon*) are heavily poached. Should the app flag CITES-listed species and decline to provide care advice on potentially illegal specimens? The horticultural ethics literature has views.
- Invasive species: in some jurisdictions, propagation or even ownership of certain plants is restricted. The recommendation engine should not encourage propagation of regulated species in user-relevant locales.
- Equity and accessibility: plant care literature has historically been written in a middle-class-Anglophone register; the app should not assume access to expensive substrates or that the user has a backyard.
- Cultural sensitivity: as noted in §7.3, some traditional common names are racist or otherwise offensive. The horticultural trade has been moving, sometimes slowly; we should be on the right side.

### 10.3 Regulation and policy

- EU AI Act — risk classification, transparency obligations for AI systems interacting with natural persons. A plant ID model is almost certainly minimal-risk, but explicit consideration is required.
- Google Play and Apple App Store policies on AI features, generative content, and accuracy claims.
- Liability for advisory output — the literature on AI advisory liability is in flux; the closest analog is consumer financial or health-advisory AI.

---

## 11. Pillar I — Market, monetization, and growth

### 11.1 The houseplant market

Indoor plant ownership has been on a multi-year secular rise; the post-pandemic plant boom is now a settled change in consumer behaviour rather than a spike. The literature on this is partly trade (*Floraculture International*, *Greenhouse Grower*, AIPH Statistical Yearbook), partly market research (Statista, IBISWorld, eMarketer reports — paywalled but the library probably has institutional access), partly academic (consumer behaviour journals).

- Market size, growth, and the houseplant segment within the broader floriculture market.
- Geographic distribution of plant ownership; demographic shifts (millennial and Gen Z plant ownership patterns).
- Retail channel: big-box, garden centres, specialty nurseries, direct-to-consumer e-commerce (The Sill, Bloomscape, niche aroid sellers).
- Adjacent retail markets: containers, substrates, fertilizers, plant-care tools. Potential affiliate or partnership space.

### 11.2 App-category economics

- Revenue models in the plant ID category: freemium with subscription paywall (dominant), one-time purchase (rare), pay-per-identification (rare), ad-supported (low-quality reputation).
- ARPDAU, LTV, conversion-to-paid, and churn benchmarks in the lifestyle / hobby app category.
- App Store Optimization (ASO) for plant ID keywords — competitive and saturated.
- Influencer and community marketing in the plant niche — TikTok plantfluencer ecosystem; Instagram aroid community; Reddit r/houseplants culture.

### 11.3 Anchor sources

- AIPH Statistical Yearbook of the International Association of Horticultural Producers — annual.
- *Floriculture International* (trade journal).
- data.ai (formerly App Annie), Sensor Tower — app analytics.
- App Store Optimization Reports — RevenueCat State of Subscriptions reports.
- Academic: *Journal of Consumer Research*; *Journal of Retailing* on hobby goods.

---

## 12. Pillar J — Adjacent and bonus topics

The following sit at the edge of the project's first version but are worth touching during the literature review because they are likely to be raised by stakeholders, will shape product roadmaps, or are quietly load-bearing for the first version even if we don't ship them.

### 12.1 Sensors and the future

- Soil moisture sensors (capacitive vs. resistive), light sensors (PAR, lux), and smartphone-based proxies for the same.
- The cottage industry of Bluetooth plant sensors (Xiaomi MiFlora, Parrot Flower Power historically) — what worked, what didn't, why most failed commercially.

### 12.2 Augmented reality

- ARCore capabilities; placing a virtual plant in a user's space to preview growth; using AR to guide camera framing for identification.
- Recent papers on AR-assisted image capture for fine-grained classification.

### 12.3 Community features

- The literature on healthy online community design; what makes plant communities specifically healthy (r/houseplants, Reddit's aroid communities, Discord servers).
- Moderation tooling, abuse vectors, the specific problem of plant rarity bragging and theft (aroid theft from botanical gardens is a real phenomenon, traceable through community posts).

### 12.4 Behavioural design and habit formation

- If we add care reminders, what does the literature say about reminder pacing that doesn't become noise?
- BJ Fogg's *Tiny Habits*, Wendy Wood's *Good Habits, Bad Habits*, and the specific literature on plant care as a habit (some surprisingly relevant horticultural-therapy work).

### 12.5 Horticultural therapy and wellbeing

- Indoor plants and mental health: the actual evidence base, which is more limited and more nuanced than the popular press suggests. American Horticultural Therapy Association publications, papers by Lee, Park, and Miyazaki on the psychophysiological effects of indoor plants.
- Indoor air quality and plants: the much-cited NASA Clean Air Study (Wolverton et al., 1989) — what it actually showed, what it did not, and why dose-response considerations matter.

---

## 13. Deliverables

### 13.1 Per-pillar synthesis memos

Each pillar produces a 5–10 page synthesis memo following the template in Appendix C. The memo is the team's central output. It should be readable by an engineer, a designer, or a stakeholder without further translation, and it should be honest about what the literature does and does not say.

Memos are due on the dates in Section 14 and reviewed in cross-pillar synthesis sessions.

### 13.2 Annotated bibliography

A single Zotero library (exported as BibTeX and CSV) with at least 300 entries across all pillars, every entry tagged with pillar(s), quality tier, and a one-paragraph annotation. The lower bound is 300; we will not be surprised if the team ends at 600. Quality matters more than count.

### 13.3 Concept map and glossary

A single visual concept map (Excalidraw or Miro) showing the relationships between the pillars and the load-bearing concepts each contributes, paired with a glossary in Appendix A of this brief. The glossary should grow over the course of the review; budget two team-weeks at the end for cleanup.

### 13.4 Implementation Implications Memo (IIM)

The IIM is the team's terminal deliverable. It is a 20–30 page integrated synthesis that takes every finding from every pillar and translates it into actionable implications for the design and engineering team. It is organized not by pillar but by product decision: "What model do we ship?", "How granular is the knowledge base?", "How do we present confidence?", "What do we paywall, if anything?", and so on, with each decision section drawing from multiple pillars.

### 13.5 Risk register

A working register of risks identified during the literature review, classified by impact (low/medium/high) and tractability (mitigable/partially mitigable/structural). The register is owned by the project lead post-handoff.

### 13.6 Open questions list

Every question the literature did not answer is logged. This list seeds the project's experiment and user-research backlog. Treat unanswered questions not as failures of the review but as its most valuable output.

---

## 14. Timeline, team, cadence

### 14.1 Suggested schedule (12 weeks)

| Week | Activity | Deliverable |
| --- | --- | --- |
| 1 | Kick-off, methodology alignment, source tier calibration, Zotero setup | Library set up; first 30 sources captured |
| 2–3 | Per-pillar deep dives (parallel) | First-pass bibliography per pillar |
| 4 | Cross-pillar synthesis session #1 | Concept map draft |
| 5–6 | Pillar memos drafted (B, C, A first; others in parallel) | Drafts of 3–4 pillar memos |
| 7 | Pillar memos drafted (D, E, F) | Drafts of remaining pillar memos |
| 8 | Pillar memos drafted (G, H, I, J) | All pillar memo drafts complete |
| 9 | Cross-pillar synthesis session #2 | Pillar memos revised |
| 10 | IIM drafting | IIM v0.1 |
| 11 | IIM review and revision; risk register; open questions | IIM v0.9; risk register complete |
| 12 | Final integration, glossary cleanup, handoff | Final package handed to design / eng |

### 14.2 Roles

- **Lead** — accountable for delivery; runs the weekly cadence; integrates the IIM.
- **Botany / horticulture lead** — Pillars A, B, and the botanical content of F. Background in horticulture, botany, plant ecology, or a serious enthusiast track record.
- **ML lead** — Pillar C. Computer vision and on-device ML.
- **Android lead** — Pillar D. Modern Android with Kotlin/Compose; some on-device ML.
- **UX / product lead** — Pillars E, F (the user-facing side), and J.
- **Legal / policy lead** — Pillars G, H. Need not be a lawyer; should be willing to read regulations carefully and flag the questions that need a lawyer.
- **Market lead** — Pillar I. Often combinable with the UX lead in a smaller team.

### 14.3 Cadence

- Weekly 60-minute team sync: each pillar reports progress, blockers, recent finds; cross-pillar dependencies surfaced.
- Bi-weekly 90-minute synthesis session: deeper discussion of one or two pillars in rotation.
- Daily: optional async stand-up via shared channel; each team member posts one find of the day with a one-line summary. Keeps the team learning from each other in real time.

---

## 15. Acceptance criteria and quality bar

The literature review is considered complete when, and only when, every item below is true.

- Each pillar memo cites at least 25 sources, with at least 8 in tier T1 or T2.
- The IIM exists at version 1.0, has been reviewed by every team lead, and has a recorded handoff date to design and engineering.
- Every working hypothesis (H1–H6 in §1.3) is adjudicated in writing: supported, contradicted, qualified, or unresolved-with-recommendation.
- The risk register has at least 20 entries, each with impact and tractability scored.
- The open-questions list contains at least 30 entries, each of which can be turned into a concrete user-research or technical-spike task.
- The glossary defines every term in the IIM that is not common-English-vocabulary.
- The concept map covers all ten pillars and is rendered as an exportable image suitable for the IIM.
- The Zotero library is fully populated, tagged, and shared with design and engineering teams in read-only mode.

A literature review can always be longer. This one is considered good enough when the team can defend, in front of an engineering review, the product decisions that flow from it — not because the deck is polished, but because the underlying reading was thorough. The test is the ability to answer the question "how do you know?" without hand-waving for any non-trivial claim.

---

## Appendix A — Working glossary

Updated continuously throughout the review. Starting set:

- **Air-filled porosity (AFP).** Fraction of substrate volume occupied by air after free drainage, typically at -1 kPa matric potential. Predicts oxygen availability to roots.
- **APG IV.** Angiosperm Phylogeny Group classification, fourth revision (2016). The current consensus phylogeny of flowering plants.
- **Aroid.** Member of family Araceae; many commercially important houseplants (*Monstera*, *Philodendron*, *Pothos*, *Anthurium*, *Spathiphyllum*).
- **CAM photosynthesis.** Crassulacean Acid Metabolism — water-conserving photosynthetic pathway in many succulents and epiphytes.
- **Container capacity.** Water content of a substrate after free drainage in a container; container-geometry-dependent.
- **ECE — Expected Calibration Error.** Measure of how well a classifier's reported probabilities match its actual accuracy.
- **Epiphyte.** Plant that grows attached to another plant, deriving water and nutrients from rain, air, and debris, not from soil.
- **FGVC.** Fine-Grained Visual Classification.
- **LECA.** Lightweight Expanded Clay Aggregate.
- **Pl@ntNet.** Citizen-science plant identification project and app, led by a French academic consortium.
- **POWO.** Plants of the World Online, Kew's authoritative taxonomic resource.
- **PRISMA-ScR.** PRISMA extension for Scoping Reviews — reporting guideline for scoping reviews.
- **Substrate archetype.** A named recipe class (in this project), e.g., aroid chunky, succulent gritty.
- **Velamen.** Spongy outer tissue on the aerial roots of orchids; absorbs water and protects the inner root.

---

## Appendix B — Source annotation template

Every Zotero entry carries the following annotation in the Notes field:

```
Tier: T1 / T2 / T3 / T4 / T5 / T6
Pillar(s): A, B, C, D, E, F, G, H, I, J
One-paragraph summary (3–6 sentences) in the team member's own words; do not paste the abstract.
Extracted claims (3–5):
  C1: <claim, defensible from the source>
  C2: ...
Implications for the product (1–3 bullets):
  I1: <how this finding lands in product decision-making>
Open questions raised (0–3 bullets)
Confidence in your own reading: high / medium / low
```

---

## Appendix C — Pillar memo template

- Pillar title and lead author.
- Executive summary (≤ 200 words).
- Background — what the pillar covers, why it matters for the product.
- State of the literature — major schools, prominent debates, the small set of must-read sources.
- Adjudication of working hypotheses — for the hypotheses in §1.3 that this pillar touches.
- Implications for design and engineering — concrete, decision-shaped statements.
- Open questions — unresolved by the literature; suggested next steps (user research, experiments, expert consultation).
- Recommended further reading for engineers and designers (top 5).
- Full reference list — Zotero-exported, every entry annotated.

---

## Appendix D — Starter bibliography (curated)

The following is a curated starting set, not a complete bibliography. Each entry is described enough to find it confidently.

### D.1 Botany and taxonomy

- Plants of the World Online (POWO), Royal Botanic Gardens Kew. https://powo.science.kew.org
- Royal Horticultural Society, *Encyclopedia of Plants and Flowers* (Brickell, ed.), most recent edition.
- Stearn, W.T., *Botanical Latin*, 4th ed. — for nomenclature.
- Croat, T.B., *A Revision of Anthurium* (multiple volumes); *A Revision of Philodendron subg. Philodendron for Central America* (*Annals of the Missouri Botanical Garden*).
- Mabberley, D.J., *Mabberley's Plant-Book*, 4th ed. — single-volume reference.
- Missouri Botanical Garden Plant Finder. https://www.missouribotanicalgarden.org/plantfinder/plantfindersearch.aspx

### D.2 Substrate and horticulture

- Raviv, M. & Lieth, J.H. (eds), *Soilless Culture: Theory and Practice*, 2nd ed., Elsevier, 2019.
- De Boodt, M. & Verdonck, O., *The Physical Properties of the Substrates in Horticulture*, *Acta Horticulturae* 26, 1972.
- Argo, W.R. & Fisher, P.R., *Understanding pH Management for Container-Grown Crops*, Meister Media, 2002 (or later edition).
- Premier Tech Grower Services Technical Bulletins (industry, but technically solid).
- Bunt, A.C., *Media and Mixes for Container Grown Plants*, 2nd ed., 1988 — older but still cited for fundamentals.
- *Acta Horticulturae*, proceedings of the International Symposium on Growing Media (every 3–4 years).

### D.3 Computer vision and plant ID

- Joly, A., Goëau, H., Bonnet, P. et al. — Pl@ntNet papers, 2014–present. Begin with the most recent overview paper and snowball.
- Garcin, C. et al., *Pl@ntNet-300K: a plant image dataset with high label ambiguity and a long-tailed distribution*, NeurIPS Datasets & Benchmarks 2021.
- Van Horn, G. et al., *The iNaturalist Species Classification and Detection Dataset*, CVPR 2018, and successor papers.
- Cole, E. et al., *Are Fine-Grained Recognition Datasets Reaching Saturation?*, CVPR 2022.
- Howard, A. et al., *Searching for MobileNetV3*, ICCV 2019; MobileNetV4 paper, 2024.
- Geng, C. et al., *Recent Advances in Open Set Recognition: A Survey*, IEEE TPAMI 2021.
- Guo, C. et al., *On Calibration of Modern Neural Networks*, ICML 2017.
- Radford, A. et al., *Learning Transferable Visual Models From Natural Language Supervision* (CLIP), ICML 2021; SigLIP paper 2023.

### D.4 Mobile and on-device ML

- Android Developers documentation — developer.android.com (current).
- Jetpack Compose reference, CameraX reference, ML Kit reference.
- LiteRT (TensorFlow Lite) documentation; ONNX Runtime Mobile documentation.
- Now in Android sample project (Google) — github.com/android/nowinandroid.

### D.5 UX and HCI

- Nielsen Norman Group — collected articles on mobile UX, AI in product, onboarding.
- Material Design 3 documentation (Google).
- Google PAIR — People + AI Research Guidebook. https://pair.withgoogle.com/guidebook
- Microsoft Research, HAX Toolkit and Guidelines for Human-AI Interaction (CHI 2019).

### D.6 Recommender systems

- Ricci, F., Rokach, L. & Shapira, B. (eds), *Recommender Systems Handbook*, 3rd ed., Springer, 2022.
- Aggarwal, C.C., *Recommender Systems: The Textbook*, Springer, 2016.

### D.7 Legal, privacy, and ethics

- Creative Commons license documentation. creativecommons.org/licenses
- GBIF data use and citation guidance. gbif.org/citation-guidelines
- EU AI Act, Regulation (EU) 2024/1689 — official text and the European Commission's published guidance.
- UK GDPR / ICO guidance for app developers.
- Google Play Data Safety section, Permissions reference, and AI-Generated Content policy.

### D.8 Market

- AIPH Statistical Yearbook of the International Association of Horticultural Producers, most recent edition.
- *Floraculture International* (trade journal).
- RevenueCat State of Subscriptions reports, most recent.
- data.ai / Sensor Tower category reports (require access).

### D.9 Specialist societies and communities

- International Aroid Society (*Aroideana* journal).
- American Orchid Society (*Orchids* magazine).
- Cactus and Succulent Society of America (*Cactus and Succulent Journal*).
- International Carnivorous Plant Society.
- r/houseplants (Reddit), r/Monstera, r/HoyaAddicts, r/SansevieriaTrifasciata, r/orchids — long-form enthusiast content; treat as tier T5.

---

## 16. A closing note to the team

Read more than feels efficient. The temptation in a literature review is to converge on the first plausible synthesis and ship it. Resist. This product will live for years, and the decisions it makes — what to identify, what to recommend, how to surface uncertainty — will be felt by people who love their plants. The literature is the cheapest, fastest way for us to inherit decades of careful thinking from people who came before us. Spend it well.

When you find an open question the literature cannot close, write it down precisely. Those questions are the seeds of the experiments and user research that will follow this phase. The best literature reviews end not with closure but with a sharper sense of what still needs to be learned, and a plan to learn it.

Good luck. Take your time. Make the work proud.
