# **Technical Scoping Report: Houseplant Identification and Substrate Recommendation Engine (Android Application)**

## **Global Market Dynamics and the Economic Evolution of Ornamental Horticulture**

The global landscape of ornamental horticulture has underwent a period of significant recalibration between 2024 and 2026\. Data from the International Association of Horticultural Producers (AIPH) indicates that the industry reached a valuation of approximately $70 billion in 2024, characterized by a sector balancing resilience against macroeconomic pressures such as labor scarcity, fluctuating resource prices, and unpredictable weather patterns.1 Within this broader context, the houseplant-specific market has demonstrated robust growth, valued at USD 7.3 billion in 2024 and projected to expand to USD 11.5 billion by 2032 at a compound annual growth rate (CAGR) of 5.8%.3 This trajectory is largely sustained by a structural shift toward wellness-oriented lifestyles and the proliferation of biophilic design in hybrid work models, where indoor greenery is viewed as an essential component of domestic infrastructure rather than a discretionary luxury.3

Retail volume trends in the 2024-2025 period reveal that garden centers and nurseries remain the dominant distribution channel, though e-commerce platforms are projected to grow at a significantly faster rate of 10.05% through 2031\.5 In the United Kingdom, garden centers reported an annual growth of 3.26% in 2024, with houseplants specifically seeing a 2.33% increase despite broader challenges in discretionary spending.6 This persistent demand is increasingly driven by younger urban demographics who view plant cultivation as a skilled hobby, shifting search intent from crisis-based queries like "why is my plant dying?" toward developmental interests such as "how to repot" and "optimized fertilizers".1

| Market Indicator | 2024 Value | 2025 Forecast | 2032 Projection | CAGR (2025-2032) |
| :---- | :---- | :---- | :---- | :---- |
| Global Houseplant Market Size | USD 7.3 Billion | USD 7.9 Billion | USD 11.5 Billion | 5.8% |
| Global Ornamental Horticulture | USD 70 Billion | \- | \- | \- |
| Online Sales Growth Rate | \- | 10.05% | \- | \- |
| UK Garden Center Growth | 3.26% | \- | \- | \- |

1

The market is further segmented by plant type and light requirements. High-light plants, including succulents, cacti, and *Ficus*, currently lead the market due to the high demand for statement foliage and architectural varieties.3 Medium-light plants, encompassing *Dracaena trifasciata* (formerly *Sansevieria*), *Epipremnum* (Pothos), and *Philodendron*, remain staples of the industry.3 Small-sized plants have emerged as the leading size segment, fueled by their suitability for apartment living and constrained urban environments.3

## **Taxonomic Prevalence and Consumer Trends in 2025-2026**

The identification engine must prioritize taxa that exhibit the highest frequency in retail and consumer collections. As of 2025, the *Monstera* genus continues to occupy a central position in the hobby, with the National Garden Bureau declaring 2025 the "Year of the Monstera".8 This trend extends beyond the classic *Monstera deliciosa* to include diverse varieties such as *Monstera adansonii*, *Monstera peru*, and high-value variegated cultivars like 'Thai Constellation'.8

Contemporary trends also highlight a return to "Retro Icons" such as *Ficus benjamina* and *Dracaena*, while simultaneously embracing rare but manageable varieties.4 A notable shift in 2025 is the "Old School is New Again" movement, which has revitalized interest in 1970s-era favorites like the Spider Plant (*Chlorophytum comosum*) and various *Hoya* species.10 The "No Drama" trend further emphasizes hardy, low-maintenance plants that cater to consumers seeking a stress-free interaction with nature.10

| Trend Category | Leading Taxa and Cultivars | Consumer Motivation |
| :---- | :---- | :---- |
| **Living Artwork** | *Monstera* 'Thai Constellation', *Calathea* 'Moonlight' | Aesthetic elevation, "quiet luxury" |
| **Fruited/Utility** | *Citrus limon* 'Meyer', *Olea europaea* (Olive Tree) | Functional gardening, purpose-driven care |
| **Retro Revival** | *Ficus benjamina*, *Chlorophytum comosum*, *Dracaena* | Nostalgia, proven resilience |
| **Rare/Exotic** | *Syngonium* 'Albo', *Philodendron* 'White Wizard' | Collector status, unique variegation |
| **Low-Maintenance** | *Zamioculcas zamiifolia* (ZZ), *Epipremnum aureum* | Ease of care, "un-killable" reputation |

4

Flowering plants have also seen a resurgence, with orchids—specifically *Phalaenopsis*—stepping into the spotlight as plant parents celebrate "rebloom wins".4 Biophilic design continues to drive the demand for large, statement-making floor plants like the Fiddle Leaf Fig (*Ficus lyrata*), which serve as natural art pieces in domestic settings.7

## **Physicochemical Principles of Soilless Substrates**

The transition of the houseplant industry toward highly technical growing media requires the recommendation engine to be grounded in the physical and chemical properties of soilless culture. Unlike traditional field agriculture, soilless production utilizes engineered substrates to optimize the ratio of air, water, and solid space within the restricted volume of a container.11

### **Substrate Physics: Porosity and Air-Water Relations**

The primary physical characteristic of a substrate is its total porosity (![][image1]), which represents the volume of the substrate comprised of pores. In standard greenhouse substrates, ![][image1] ranges from 70% to 90%.13 The distribution of these pores into macropores and micropores determines the substrate's Air-Filled Porosity (AFP) and Water-Holding Capacity (WHC).

1. **Macropores (\>100 \\mu m):** These pores facilitate rapid drainage and gas exchange. After irrigation, gravity drains water from these pores, allowing air to return to the root zone.12 AFP refers to the volume of pore space occupied by air after a substrate is saturated and allowed to drain.15  
2. **Micropores (30-3 \\mu m):** These pores retain water against the pull of gravity through capillary action. Water held in pores smaller than 3 \\mu m is generally unavailable for plant uptake.14

Optimal growth occurs when a substrate provides a balance of "easily available water" and sufficient air. In commercial production, a "normal" range for soilless media is an AFP of 10-30% and a container capacity of 65-80%.16 Deviation from these ranges can lead to either chronic dehydration or root asphyxiation.

| Property | Definition | Recommended Range (Soilless) |
| :---- | :---- | :---- |
| **Total Porosity** | Volume fraction of pores | 75% – 95% |
| **Air Space (AFP)** | Pore volume filled with air after drainage | 10% – 30% |
| **Container Capacity** | Maximum water held after drainage | 65% – 80% |
| **Bulk Density** | Dry mass per unit volume | 0.09 – 1.3 g/cm³ |

13

### **The Influence of Container Geometry**

A critical nuance for the recommendation engine is the relationship between container height and substrate saturation. A substrate should be conceptualized as a sponge; shorter containers provide less gravitational head, resulting in a higher percentage of the pore space being filled with water and a lower overall AFP.12 This phenomenon implies that the same substrate will behave differently in a shallow seedling flat compared to a 6-inch (15 cm) pot.12

| Container Type | Pot Height | Air Porosity (AFP) | Water Porosity (WHC) |
| :---- | :---- | :---- | :---- |
| **Standard Pot** | 6 inch (15 cm) | 19% | 64% |
| **Standard Pot** | 4 inch (10 cm) | 13% | 70% |
| **Bedding Flat** | 3 inch (8 cm) | 7% | 76% |

12

### **Chemical Properties: pH and Cation Exchange Capacity (CEC)**

The chemical behavior of a substrate is governed by its Cation Exchange Capacity (CEC), which measures the capacity to hold exchangeable cations such as ![][image2], ![][image3], ![][image4], and ![][image5].15 High CEC components include peat moss, coconut coir, and vermiculite, which act as nutrient reservoirs.15 Low CEC materials like perlite and sand provide structure but do not contribute to nutrient retention.17

Substrate pH is the primary determinant of nutrient availability. Most greenhouse crops thrive in a slightly acidic range of 5.4 to 6.6 in soilless media.15 In alkaline substrates, micronutrients like iron, copper, and zinc often become deficient, while acidic substrates can lead to toxic levels of aluminum or manganese.15

## **Substrate Component Analysis and Transition to Peat-Free Media**

The 2026 horticultural landscape is defined by the transition away from peat-based media. The Royal Horticultural Society (RHS) implemented a "no new peat" policy across its retail and garden operations on January 1, 2026, setting a standard for the UK industry.18 This shift is motivated by the environmental degradation associated with peat extraction, which destroys vital carbon sinks and ecosystems.18

### **Organic Constituents**

**Coconut Coir:** A primary peat alternative derived from processed coconut husks. Coir provides moderate to high CEC and excellent water retention, holding up to 30% more water than peat moss.19 It is structurally stable but often requires supplemental nutrients compared to peat.19

**Orchid Bark:** Typically made from fir or pine bark, this chunky material is essential for increasing aeration and drainage. High-quality bark, such as *Pinus pinaster* (Orchiata), is favored for its rounded shape and resistance to breakdown, which typically occurs over 18-24 months.21

**Sphagnum Moss:** Often used as a "classic" additive to increase moisture retention in orchid and propagation mixes. Its large body of nutrient solution can delay disadvantageous changes in concentration during cultivation.22

### **Inorganic and Mineral Amendments**

**Perlite:** A lightweight, expanded volcanic glass used to prevent compaction and increase drainage. It has high permeability but low water retention.19

**Pumice:** A superior alternative to perlite in many "aroid" mixes due to its structural weight and ability to hold both air and moisture within its porous structure without floating to the surface.20

**Horticultural Charcoal:** A pure, porous carbon source that absorbs odors, impurities, and excess moisture while providing long-term structural macroporosity (5-10 years).21

| Component | CEC Level | pH Range | Decomposition Rate |
| :---- | :---- | :---- | :---- |
| **Peat Moss** | High | 3.0 – 4.5 | Moderate |
| **Coco Coir** | High | 5.5 – 6.8 | Slow |
| **Orchid Bark** | Moderate | 4.0 – 5.0 | Slow (18-24 months) |
| **Pumice** | Low | 7.0 – 8.0 | Very Slow (5+ years) |
| **Perlite** | Negligible | 7.0 – 7.5 | Very Slow (5+ years) |
| **Charcoal** | Moderate | 7.0 – 9.0 | Extremely Slow (10+ years) |

14

## **Specialized Substrate Archetypes and Family-Specific Recipes**

The recommendation engine must tailor substrate compositions based on the root traits and microhabitat requirements of specific plant families.

### **Araceae and the "Chunky Aroid Mix"**

Aroids, including *Monstera*, *Philodendron*, and *Anthurium*, are often hemiepiphytes that require oxygen-rich root zones. Standard nursery mixes (peat and perlite) often suffocate these roots over time as the media compacts.25 A successful aroid mix balances moisture with fast re-aeration through a "chunky" texture.20

**Standard Aroid Recipe:**

* **5 parts Orchid Bark:** Provides structure and mimics tree trunks.  
* **4 parts Coco Coir:** Retains essential moisture and nutrients.  
* **5 parts Pumice/Perlite:** Ensures permanent air pathways.  
* **2 parts Horticultural Charcoal:** Keeps the mix "fresh" and absorbs salts.  
* **2 parts Worm Castings:** Provides a mild, biology-friendly nutrient source.

20

### **Orchidaceae and the "Epiphytic Bark Mix"**

Orchids require high AFP due to their aerial root systems, which are designed to absorb moisture from rain and dry out rapidly.22 Substrate needs vary by family:

* **Cattleya:** Requires maximum drainage; 50% medium bark, 30% clay pebbles/lava rock, 20% charcoal.27  
* **Phalaenopsis:** Needs balanced moisture; 40% bark, 30% sphagnum moss, 20% perlite, 10% charcoal.27  
* **Paphiopedilum:** Prefers a moisture-retentive, slightly alkaline mix; addition of dolomite lime is recommended to adjust pH.27

### **Carnivorous Plants and the "Bog Archetype"**

Carnivorous species like *Dionaea muscipula* (Venus Flytrap) and *Sarracenia* evolved in nutrient-poor bogs and are highly sensitive to minerals. Their substrate must be completely inert and acidic, typically a 50:50 mix of long-fiber sphagnum or pure peat and washed horticultural sand/perlite.28 The recommendation engine must strictly warn against the use of fortified soils (e.g., Miracle-Gro) or tap water, which contains dissolved salts that can be lethal to these species.29

## **Physiological Nuances: Cultivars and Variegation Management**

The application must differentiate between standard green species and variegated cultivars, as the lack of chlorophyll significantly alters metabolic rates and substrate requirements.

### **Slower Transpiration and Risk of Saturation**

Variegated plants, such as *Monstera* 'Albo' or *Philodendron* 'Pink Princess', possess chlorophyll-deficient tissue that cannot photosynthesize efficiently.30 Consequently, these plants grow more slowly and utilize water more gradually than their fully green counterparts.30 This slower transpiration increases the risk of the substrate remaining saturated for excessive periods, leading to root rot.30 Substrate recommendations for these cultivars should lean toward a higher percentage of chunky materials (bark, pumice) to facilitate rapid drying.31

### **Light Compensation and Substrate Interaction**

Variegated leaves require higher light levels (bright indirect light) to compensate for reduced chlorophyll.30 If light levels are insufficient, the plant may "revert" to green or shed variegated sections to conserve energy.31 The recommendation engine should link substrate choice with ambient light conditions; plants in lower light settings should be placed in even more porous, well-draining mixes to account for reduced metabolic activity.21

## **Computer Vision and Plant Identification Architecture**

The core of the application—the visual identification engine—leverages advancements in fine-grained classification and multi-label recognition tailored for ecological and domestic contexts.

### **PlantCLEF Benchmarks and the "Domain Shift" Challenge**

The identification task is complicated by "domain shift." While training data typically consists of 1.4 million high-quality, single-species images from citizen-science sources (Pl@ntNet), real-world user photos are often complex "quadrats" containing multiple overlapping plants, shadows, and occlusions.36

The PlantCLEF 2025 challenge results demonstrated that state-of-the-art (SOTA) performance is achieved using the **DINOv2** pretrained vision transformer combined with multi-scale tiling strategies.36 Preprocessing, specifically alignment of compression settings and the use of Lanczos interpolation for resizing, proved critical for maintaining accuracy.36

| Identification Metric | 2024 Performance | 2025 Best Performance |
| :---- | :---- | :---- |
| **Sample-averaged F1 Score** | 0.29 | 0.35 |
| **Core Architecture** | CNN / Early ViT | DINOv2 (Vision Transformer) |
| **Training Images** | 1.4 Million | 1.4 Million (plus unannotated quadrats) |
| **Species Coverage** | \~7,800 Taxa | \~7,800 Taxa |

36

### **Mobile-Optimized Models: MobileNetV4**

For on-device inference on Android, the **MobileNetV4 (MNv4)** suite represents the current Pareto frontier across CPUs, GPUs, and NPUs.39 MNv4 introduces the Universal Inverted Bottleneck (UIB) and Mobile Multi-Query Attention (MQA), delivering significant speedups on mobile accelerators like the Pixel EdgeTPU.39

![][image6]  
![][image7]  
39

This architecture is twice as fast as MobileNetV3 and significantly outperforms competitors like FastViT and MobileOne in equivalent accuracy targets on specialized hardware.39

## **Android Implementation and Edge AI Frameworks**

The 2026 application development stack centers on **LiteRT** (formerly TensorFlow Lite), Google's framework for high-performance machine learning on mobile devices.42

### **LiteRT and Hardware Acceleration**

LiteRT leverages the **CompiledModel API**, which optimizes hardware acceleration by pre-compiling models for specific SoCs.43 The **ML Drift** GPU engine provides a 1.4x faster inference rate over legacy delegates, while NPU acceleration offers an additional 3x performance gain for compute-heavy prefill tasks in generative components.44

### **Real-Time Inference with CameraX**

The implementation utilize **CameraXViewfinder** in **EMBEDDED** mode (TextureView-backed) within a Jetpack Compose environment.45 This entirely eliminates the lifecycle mismatch of legacy interop approaches and allows for native Compose modifiers, such as RenderEffect for blur or alpha transitions, to be applied to the camera stream.45

1. **Coordinate Mapping:** The use of MutableCoordinateTransformer streamlines mapping between camera sensor coordinates and UI coordinates, facilitating "tap-to-identify" features.45  
2. **Thread Management:** CameraX handles blocking interprocess communication (IPC) with hardware on background threads, ensuring the UI remains fluid even during intensive inference cycles.46

## **Regulatory and Sustainability Frameworks in 2026**

The application must operate within the legal and ethical boundaries established by the UK peat legislation and the European Union’s AI regulations.

### **The UK Peat Ban and "No New Peat" Standards**

Despite political shifts and "U-turns" in government legislation, the UK horticulture industry has moved toward a voluntary ban on peat in retail.47 The RHS "no new peat" standard is the most rigorous, defining acceptable plants as those grown 100% peat-free or containing only historical peat from before the end of 2025\.18 The application's recommendation engine should default to peat-free components (Coir, Wood Fiber, Bark) to align with these industry standards and environmental objectives.18

### **EU AI Act: Risk Classification and Compliance**

Under the **EU AI Act** (enforced August 2026), AI systems are classified into four risk tiers.49 A plant identification and advice application generally falls under:

* **Minimal Risk:** Most features have no mandatory obligations beyond general AI literacy.50  
* **Limited Risk:** If the app uses a virtual assistant or generates synthetic content (e.g., AI-generated disease diagnostics), it must comply with transparency obligations by disclosing that the output is AI-generated (Article 50).50  
* **High-Risk Triggers:** The app could enter the high-risk category if misidentification poses a significant risk to human health or safety, particularly in identifying toxic species.49

### **Liability and the 2024 Product Liability Directive (PLD)**

The **2024 Product Liability Directive** establishes strict liability for AI-enabled products.54 If the application misidentifies a toxic plant and causes "autonomous harm" (e.g., to a pet or child), the "deployer" (the app developer) is responsible.54 Courts may presume the product was defective if the claimant faces technical complexity in proving fault, shifting the burden of proof to the developer.54

## **Safety and Toxicity Database Integration**

The recommendation engine must incorporate a safety layer based on ASPCA and RHS toxicity data to alert users to potential risks.

| Plant Taxon | Scientific Name | Toxicity Rank (Cats/Dogs) | Symptoms of Ingestion |
| :---- | :---- | :---- | :---- |
| **Sago Palm** | *Cycas revoluta* | Severe / Fatal | Liver failure, seizures, death |
| **Easter Lily** | *Lilium longiflorum* | Fatal (Cats Only) | Acute kidney failure |
| **Oleander** | *Nerium oleander* | Severe | Cardiac arrhythmia, low blood pressure |
| **Dumb Cane** | *Dieffenbachia spp.* | Moderate | Oral swelling, difficulty breathing |
| **Pothos** | *Epipremnum aureum* | Mild to Moderate | Oral irritation, vomiting |
| **Fiddle Leaf Fig** | *Ficus lyrata* | Mild | GI upset, sap causes skin irritation |

57

The application must also provide a list of "Pet-Friendly" alternatives, such as African Violets (*Saintpaulia*), Boston Ferns (*Nephrolepis exaltata*), and Spider Plants (*Chlorophytum comosum*).58

## **Competitive Benchmarking and User Feature Expectations**

A review of top-tier applications—**PictureThis**, **PlantIn**, **PlantNet**, and **iNaturalist**—identifies the features that define market leaders in 2026\.

### **Accuracy and Trust**

PictureThis remains the accuracy champion, with verified benchmarks around 78% compared to PlantNet's 68%.61 However, user trust is increasingly tied to "explainability" and the presence of community verification (iNaturalist) or expert botanical support (PlantIn).61

### **Feature Sets**

| App Feature | Leading Competitor | Benefit to User |
| :---- | :---- | :---- |
| **Instant Identification** | PictureThis | Speed and convenience |
| **Expert Support** | PlantIn | High confidence in diagnosis |
| **Biometric Diagnostics** | Plant Parent | Early detection of pests/diseases |
| **Community Feed** | iNaturalist | Learning and data sharing |
| **Peat-Free Guidance** | (Niche) | Environmental alignment |

61

One significant gap identified in current apps is the lack of "substrate-specific" guidance. While most provide watering reminders, few offer precise substrate ratios tailored to the plant's root physiology and pot geometry.63 This represents a primary opportunity for the proposed engine.

## **Implementation Roadmap and Strategic Conclusions**

The successful delivery of the Houseplant Identification and Substrate Recommendation Engine requires a convergence of horticultural science, edge AI, and regulatory vigilance.

### **AI Model Refinement**

1. **Multi-Label Training:** The model must be trained on "quadrat-style" unannotated field data using self-supervised learning to bridge the domain shift from citizen-science close-ups.36  
2. **MNv4 Deployment:** Utilizing MobileNetV4 architectures ensures that identification is instantaneous, preserving the "magic" of real-time discovery.39  
3. **Uncertainty Awareness:** Implement model calibration metrics like **Expected Calibration Error (ECE)** to ensure that confidence scores reflect true probabilities, especially for toxic plant identification.66

### **Horticultural Logic and UX**

1. **Geometric Inputs:** The substrate engine should prompt for pot height to adjust the recommended mix for gravitational saturation.12  
2. **Sustainability Scoring:** Mixes should be ranked by their peat-free status and decomposition longevity, aiding the transition toward eco-conscious gardening.7  
3. **Metabolic Logic:** Recommendations for variegated plants must automatically include a \+10-15% increase in mineral aggregates (pumice/perlite) to account for slower transpiration rates.31

### **Compliance and Safety**

1. **Auditability:** Every identification and advice output should be logged and traceable to ensure compliance with the 2026 EU AI Liability guidelines.56  
2. **Dynamic Risk Assessment:** Implement protocols where the AI agent "stops" and prompts for expert intervention when high-risk toxic species are detected with low confidence.56

The scoping document version 1.0 establishes a profound foundation for the literature review team. By focusing on the intersection of substrate physical properties, mobile-first AI architectures, and the shifting legislative landscape of 2026, the project team is positioned to deliver a unique and essential tool for the modern horticultural consumer.

#### **Works cited**

1. International Statistics Flowers & Plants 2024: Trusted Resource for the Global Horticulture Industry \- AIPH, accessed on May 11, 2026, [https://aiph.org/latest-news/international-statistics-flowers-plants-2024-trusted-resource-for-the-global-horticulture-industry/](https://aiph.org/latest-news/international-statistics-flowers-plants-2024-trusted-resource-for-the-global-horticulture-industry/)  
2. Crucial Global Data Released—International Statistics Flowers and Plants 2025 \- AIPH, accessed on May 11, 2026, [https://aiph.org/latest-news/crucial-global-data-released-international-statistics-flowers-and-plants-2025/](https://aiph.org/latest-news/crucial-global-data-released-international-statistics-flowers-and-plants-2025/)  
3. Houseplants Market | Size, Share, Volume 2026 to 2033, accessed on May 11, 2026, [https://www.statsmarketresearch.com/global-houseplants-forecast-market-8067085](https://www.statsmarketresearch.com/global-houseplants-forecast-market-8067085)  
4. The Sill 2025 Plant Trend Report & 2026 Predictions, accessed on May 11, 2026, [https://www.thesill.com/blogs/ask-the-sill/the-sill-2025-plant-trend-report-2026-predictions](https://www.thesill.com/blogs/ask-the-sill/the-sill-2025-plant-trend-report-2026-predictions)  
5. Houseplant Statistics 2026: Market Size & Industry Trends \- Terrarium Tribe, accessed on May 11, 2026, [https://terrariumtribe.com/houseplant-statistics/](https://terrariumtribe.com/houseplant-statistics/)  
6. Positive growth for UK garden centres in 2024, reports GCA \- AIPH, accessed on May 11, 2026, [https://aiph.org/floraculture/news/positive-growth-for-uk-garden-centres-in-2024-reports-gca/](https://aiph.org/floraculture/news/positive-growth-for-uk-garden-centres-in-2024-reports-gca/)  
7. Houseplants Trends for 2025: What's Growing Popular?, accessed on May 11, 2026, [https://www.happyhouseplants.co.uk/blogs/houseplant-blog/houseplants-trends-for-2025-whats-growing-popular](https://www.happyhouseplants.co.uk/blogs/houseplant-blog/houseplants-trends-for-2025-whats-growing-popular)  
8. Houseplant Trends for 2025 \- City Floral Garden Center \- Denver Colorado, accessed on May 11, 2026, [https://cityfloralgreenhouse.com/2025/01/houseplant-trends-for-2025/](https://cityfloralgreenhouse.com/2025/01/houseplant-trends-for-2025/)  
9. What Were the Most Popular Houseplants in 2025? According to These Experts, This Is What Everyone Was Growing This Year \- Livingetc, accessed on May 11, 2026, [https://www.livingetc.com/advice/most-loved-houseplants-of-2025](https://www.livingetc.com/advice/most-loved-houseplants-of-2025)  
10. 2025's Houseplant Trends and Trending Indoor Plants \- lovethatleaf, accessed on May 11, 2026, [https://www.lovethatleaf.co.nz/blogs/plant-care-guides/2025-houseplant-trends-trending-indoor-plants](https://www.lovethatleaf.co.nz/blogs/plant-care-guides/2025-houseplant-trends-trending-indoor-plants)  
11. SOILLESS CULTURE \- Heiner Lieth, accessed on May 11, 2026, [https://lieth.ucdavis.edu/pub/pub071\_ravivlieth\_soillessculture\_book.pdf](https://lieth.ucdavis.edu/pub/pub071_ravivlieth_soillessculture_book.pdf)  
12. Understanding pH management and plant nutrition \- Part 4: Substrates \- St. Augustine Orchid Society, accessed on May 11, 2026, [https://staugorchidsociety.org/PDF/IPASubstrates.pdf](https://staugorchidsociety.org/PDF/IPASubstrates.pdf)  
13. Unit 07: Substrates \- Greenhouse Management Online, accessed on May 11, 2026, [https://greenhouse.hosted.uark.edu/Unit07/Printer\_Friendly.html](https://greenhouse.hosted.uark.edu/Unit07/Printer_Friendly.html)  
14. (PDF) Physico-chemical and physical properties of some substrates used in horticulture, accessed on May 11, 2026, [https://www.researchgate.net/publication/390201538\_Physico-chemical\_and\_physical\_properties\_of\_some\_substrates\_used\_in\_horticulture](https://www.researchgate.net/publication/390201538_Physico-chemical_and_physical_properties_of_some_substrates_used_in_horticulture)  
15. Growing Media for Greenhouse Crops: Properties, accessed on May 11, 2026, [https://www.greenhouse-management.com/greenhouse\_management/growing\_media\_greenhouse\_crops/properties\_growing\_media.htm](https://www.greenhouse-management.com/greenhouse_management/growing_media_greenhouse_crops/properties_growing_media.htm)  
16. Air Porosity and Water-Holding Ability of Media Components \- Sun Gro, accessed on May 11, 2026, [https://www.sungro.com/air-porosity-and-water-holding-ability-of-media-components/](https://www.sungro.com/air-porosity-and-water-holding-ability-of-media-components/)  
17. Commercial Greenhouse and Nursery Production \- Purdue Extension, accessed on May 11, 2026, [https://www.extension.purdue.edu/extmedia/HO/HO-255-W.pdf](https://www.extension.purdue.edu/extmedia/HO/HO-255-W.pdf)  
18. RHS peat policy / RHS, accessed on May 11, 2026, [https://www.rhs.org.uk/about-us/what-we-do/policies/rhs-statement-on-peat](https://www.rhs.org.uk/about-us/what-we-do/policies/rhs-statement-on-peat)  
19. The Best DIY Chunky Soil Mix for Aroids \- Cori Sears, accessed on May 11, 2026, [https://corisears.ca/diy-chunky-soil-mix-for-aroids/](https://corisears.ca/diy-chunky-soil-mix-for-aroids/)  
20. My Best DIY Aroid Mix Recipe \- Chunky and Soil-Free \- The Plant Puddle, accessed on May 11, 2026, [https://theplantpuddle.org/blogs/plant-care-blog/chunky-potting-mix-for-aroids-and-houseplants](https://theplantpuddle.org/blogs/plant-care-blog/chunky-potting-mix-for-aroids-and-houseplants)  
21. The Complete Aroid Substrate Guide: Match Mix to Roots \- Foliage Factory, accessed on May 11, 2026, [https://foliage-factory.com/blogs/plant-care/aroid-substrate-guide](https://foliage-factory.com/blogs/plant-care/aroid-substrate-guide)  
22. Choose the Best Substrate for Phalaenopsis Pot Plants | Anthura, accessed on May 11, 2026, [https://anthura.nl/en/crop-optimization/phalaenopsis-pot-advice/substrate-phalaenopsis/](https://anthura.nl/en/crop-optimization/phalaenopsis-pot-advice/substrate-phalaenopsis/)  
23. Orchid Bark Guide: Choosing the Right Bark for Your Orchids \- Harwood's Garden Supplies, accessed on May 11, 2026, [https://www.harwoodsgardensupplies.com.au/blogs/blog/orchid-bark-guide-choosing-the-right-bark-for-your-orchids](https://www.harwoodsgardensupplies.com.au/blogs/blog/orchid-bark-guide-choosing-the-right-bark-for-your-orchids)  
24. Soilless Culture: Theory and Practice | Request PDF \- ResearchGate, accessed on May 11, 2026, [https://www.researchgate.net/publication/298462357\_Soilless\_Culture\_Theory\_and\_Practice](https://www.researchgate.net/publication/298462357_Soilless_Culture_Theory_and_Practice)  
25. My DIY Aroid Soil Mix Recipe \- Hoya Treasures, accessed on May 11, 2026, [https://hoyatreasures.com/blogs/houseplant-care/my-diy-aroid-soil-mix-recipe](https://hoyatreasures.com/blogs/houseplant-care/my-diy-aroid-soil-mix-recipe)  
26. (PDF) Physico-chemical and physical properties of some substrates used in horticulture, accessed on May 11, 2026, [https://www.researchgate.net/publication/271177653\_Physico-chemical\_and\_physical\_properties\_of\_some\_substrates\_used\_in\_horticulture](https://www.researchgate.net/publication/271177653_Physico-chemical_and_physical_properties_of_some_substrates_used_in_horticulture)  
27. Orchid Medium Mixes for the best care \- The Dark Orchid, accessed on May 11, 2026, [https://www.thedarkorchid.com/post/the-best-growing-medium-mixes-for-each-orchid-family](https://www.thedarkorchid.com/post/the-best-growing-medium-mixes-for-each-orchid-family)  
28. Carnivorous Plants: Types, Care, and How to Grow Them \- Costa Farms, accessed on May 11, 2026, [https://costafarms.com/blogs/get-growing/carnivorous-plants-care-types](https://costafarms.com/blogs/get-growing/carnivorous-plants-care-types)  
29. General Carnivorous Plant Growing Tips, accessed on May 11, 2026, [https://www.californiacarnivores.com/blogs/growing-tips/76003845-general-carnivorous-plant-growing-tips](https://www.californiacarnivores.com/blogs/growing-tips/76003845-general-carnivorous-plant-growing-tips)  
30. Monstera Albo Care Guide for Variegated Growth | Urbane Eight, accessed on May 11, 2026, [https://urbaneeight.com/blogs/news/monstera-albo-variegata-care-guide](https://urbaneeight.com/blogs/news/monstera-albo-variegata-care-guide)  
31. Variegated Plants: Myths, Science, and Stunning Foliage Explained, accessed on May 11, 2026, [https://foliage-factory.com/blogs/plant-care/variegated-plants-explained](https://foliage-factory.com/blogs/plant-care/variegated-plants-explained)  
32. Monstera Albo Variegata: 7+ Rare Facts That Justify the Hype \- Leafy Heaven, accessed on May 11, 2026, [https://www.leafyheaven.com/monstera-albo-variegata/](https://www.leafyheaven.com/monstera-albo-variegata/)  
33. Growing Monstera deliciosa/Borsigiana albo \- Strange Wonderful Things, accessed on May 11, 2026, [https://www.strangewonderfulthings.com/tips114.htm](https://www.strangewonderfulthings.com/tips114.htm)  
34. How to Plant, Grow, and Care for Monstera Albo \- Complete Guide \- Planet Natural, accessed on May 11, 2026, [https://www.planetnatural.com/monstera-albo/](https://www.planetnatural.com/monstera-albo/)  
35. Pink Princess Philodendron \- Plant Care 101 \- Gardenia.net, accessed on May 11, 2026, [https://www.gardenia.net/plant/philodendron-pink-princess](https://www.gardenia.net/plant/philodendron-pink-princess)  
36. PlantCLEF 2025: Advancing AI-based Multi-Species Plant ..., accessed on May 11, 2026, [https://archive.org/details/plantclef2025ad9mart](https://archive.org/details/plantclef2025ad9mart)  
37. PlantCLEF2025 @ LifeCLEF & CVPR-FGVC \- Kaggle, accessed on May 11, 2026, [https://www.kaggle.com/competitions/plantclef-2025/data](https://www.kaggle.com/competitions/plantclef-2025/data)  
38. PlantCLEF2025 @ LifeCLEF & CVPR-FGVC \- Kaggle, accessed on May 11, 2026, [https://www.kaggle.com/competitions/plantclef-2025](https://www.kaggle.com/competitions/plantclef-2025)  
39. MobileNetV4 \- Universal Models for the Mobile Ecosystem \- arXiv, accessed on May 11, 2026, [https://arxiv.org/html/2404.10518v1](https://arxiv.org/html/2404.10518v1)  
40. MobileNetV4: Universal Models for the Mobile Ecosystem \- ECVA | European Computer Vision Association, accessed on May 11, 2026, [https://www.ecva.net/papers/eccv\_2024/papers\_ECCV/papers/05647.pdf](https://www.ecva.net/papers/eccv_2024/papers_ECCV/papers/05647.pdf)  
41. Papers Explained 232: MobileNetV4 | by Ritvik Rastogi \- Medium, accessed on May 11, 2026, [https://ritvik19.medium.com/papers-explained-232-mobilenetv4-83a526887c30](https://ritvik19.medium.com/papers-explained-232-mobilenetv4-83a526887c30)  
42. LiteRT: High-Performance On-Device Machine Learning Framework | Google AI Edge, accessed on May 11, 2026, [https://ai.google.dev/edge/litert](https://ai.google.dev/edge/litert)  
43. LiteRT overview | Google AI Edge | Google AI for Developers, accessed on May 11, 2026, [https://ai.google.dev/edge/litert/overview](https://ai.google.dev/edge/litert/overview)  
44. LiteRT: The Universal Framework for On-Device AI \- Google for Developers Blog, accessed on May 11, 2026, [https://developers.googleblog.com/litert-the-universal-framework-for-on-device-ai/](https://developers.googleblog.com/litert-the-universal-framework-for-on-device-ai/)  
45. Compose-Native CameraX in 2026: The Complete Guide | by Ioannis Anifantakis \- ProAndroidDev, accessed on May 11, 2026, [https://proandroiddev.com/compose-native-camerax-in-2026-the-complete-guide-bf36c76a78e9](https://proandroiddev.com/compose-native-camerax-in-2026-the-complete-guide-bf36c76a78e9)  
46. Configuration options | Android media \- Android Developers, accessed on May 11, 2026, [https://developer.android.com/media/camera/camerax/configuration](https://developer.android.com/media/camera/camerax/configuration)  
47. All RHS Retail to sell only 'no new peat' plants from January 2026 / RHS Gardening, accessed on May 11, 2026, [https://www.rhs.org.uk/advice/peat/rhs-retail-peat-free-from-2026](https://www.rhs.org.uk/advice/peat/rhs-retail-peat-free-from-2026)  
48. Ban peat: legislation shouldn't be this hard | Jack Wallington | Nature & Gardens, accessed on May 11, 2026, [https://www.jackwallington.com/legislation-shouldnt-be-this-difficult/](https://www.jackwallington.com/legislation-shouldnt-be-this-difficult/)  
49. EU AI Act: Risk-Classifications of the AI Regulation \- trail AI governance platform, accessed on May 11, 2026, [https://www.trail-ml.com/blog/eu-ai-act-how-risk-is-classified](https://www.trail-ml.com/blog/eu-ai-act-how-risk-is-classified)  
50. EU AI Act Risk Classification Playbook: Prohibited vs High-Risk vs Limited-Risk, accessed on May 11, 2026, [https://www.glocertinternational.com/resources/guides/eu-ai-act-risk-classification-playbook/](https://www.glocertinternational.com/resources/guides/eu-ai-act-risk-classification-playbook/)  
51. U.S. Companies Face EU AI Act's Possible August 2026 Compliance Deadline | Insights, accessed on May 11, 2026, [https://www.hklaw.com/en/insights/publications/2026/04/us-companies-face-eu-ai-acts-possible-august-2026-compliance-deadline](https://www.hklaw.com/en/insights/publications/2026/04/us-companies-face-eu-ai-acts-possible-august-2026-compliance-deadline)  
52. AI Risk Classification: Guide to EU AI Act Risk Categories \- GDPR Local, accessed on May 11, 2026, [https://gdprlocal.com/ai-risk-classification/](https://gdprlocal.com/ai-risk-classification/)  
53. Navigating the AI Act | Shaping Europe's digital future \- European Union, accessed on May 11, 2026, [https://digital-strategy.ec.europa.eu/en/faqs/navigating-ai-act](https://digital-strategy.ec.europa.eu/en/faqs/navigating-ai-act)  
54. AI Liability in light of the new 2024 PLD: expanded liability, challenging defences, and new evidentiary burdens \- Bird & Bird, accessed on May 11, 2026, [https://www.twobirds.com/en/insights/2026/france/ai-liability-in-light-of-the-new-2024-pld-expanded-liability-challenging-defences-and-new-evidentiar](https://www.twobirds.com/en/insights/2026/france/ai-liability-in-light-of-the-new-2024-pld-expanded-liability-challenging-defences-and-new-evidentiar)  
55. Navigating product liability in high-security sectors: Addressing AI-driven risks under German and European law | White & Case LLP, accessed on May 11, 2026, [https://www.whitecase.com/insight-alert/navigating-product-liability-high-security-sectors-addressing-ai-driven-risks-under](https://www.whitecase.com/insight-alert/navigating-product-liability-high-security-sectors-addressing-ai-driven-risks-under)  
56. AI Liability 2026: Who is responsible for AI agent mistakes? \- PrudAI, accessed on May 11, 2026, [https://prudai.com/blog/ai-liability-who-is-responsible-when-an-agent-makes-a-mistake](https://prudai.com/blog/ai-liability-who-is-responsible-when-an-agent-makes-a-mistake)  
57. These Houseplants Can Cause Trouble for Your Pets \- ASPCA, accessed on May 11, 2026, [https://www.aspca.org/news/these-houseplants-can-cause-trouble-your-pets](https://www.aspca.org/news/these-houseplants-can-cause-trouble-your-pets)  
58. 25 Common Plants Poisonous to Cats | ASPCA Pet Health Insurance, accessed on May 11, 2026, [https://www.aspcapetinsurance.com/resources/plants-poisonous-to-cats/](https://www.aspcapetinsurance.com/resources/plants-poisonous-to-cats/)  
59. Do You Know Which Flowers and Plants are Toxic to Pets? Our Experts Explain\! | ASPCA, accessed on May 11, 2026, [https://www.aspca.org/news/do-you-know-which-flowers-and-plants-are-toxic-pets-our-experts-explain](https://www.aspca.org/news/do-you-know-which-flowers-and-plants-are-toxic-pets-our-experts-explain)  
60. Is That Houseplant Safe for Your Pets? \- ASPCA, accessed on May 11, 2026, [https://www.aspca.org/news/houseplant-safe-your-pets](https://www.aspca.org/news/houseplant-safe-your-pets)  
61. Plant Identification App Reviews That Save You From Wrong IDs \- Shop, accessed on May 11, 2026, [https://www.shop.bottegadelsarto.com/feed/plant-identification-app-reviews-737785](https://www.shop.bottegadelsarto.com/feed/plant-identification-app-reviews-737785)  
62. Testing Plant Identification Apps – Which Is Best? \- GrowIt BuildIT, accessed on May 11, 2026, [https://growitbuildit.com/plant-identification-apps-tested/](https://growitbuildit.com/plant-identification-apps-tested/)  
63. Plantin vs PictureThis Apps Review, accessed on May 11, 2026, [https://myplantin.com/blog/plantin-vs-picturethis](https://myplantin.com/blog/plantin-vs-picturethis)  
64. The Best Plant ID App \- We Tested 7 Different Ones \- Garden Myths, accessed on May 11, 2026, [https://www.gardenmyths.com/the-best-plant-id-app-we-tested-7-different-ones/](https://www.gardenmyths.com/the-best-plant-id-app-we-tested-7-different-ones/)  
65. The Best Plant Identification Apps of 2026 \- PlantIn, accessed on May 11, 2026, [https://myplantin.com/blog/best-plant-identification-apps](https://myplantin.com/blog/best-plant-identification-apps)  
66. Understanding Model Calibration \- A gentle introduction and visual exploration of calibration and the expected calibration error (ECE) \- arXiv, accessed on May 11, 2026, [https://arxiv.org/html/2501.19047v2](https://arxiv.org/html/2501.19047v2)  
67. Expected Calibration Error (ECE): A Step-by-Step Visual Explanation, accessed on May 11, 2026, [https://towardsdatascience.com/expected-calibration-error-ece-a-step-by-step-visual-explanation-with-python-code-c3e9aa12937d/](https://towardsdatascience.com/expected-calibration-error-ece-a-step-by-step-visual-explanation-with-python-code-c3e9aa12937d/)

[image1]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABIAAAAaCAYAAAC6nQw6AAABMUlEQVR4Xu2RPy9EURBHR0SCiAKV0IhGpdgot1MQodJpdFv6CDQ+AKGxtvQRVqXQSWg0ohAF8SfRkaxik8UZ8x73jc1zHyUnOcm++U0md2ZF/inKLO5gDU8T9bfW1BUc/OjOYQIXcQ9fcTn5Tm1iA+eS/m/ZFBvkmcEXvMBRl32hDw+l/aCS2Is00zPksiDWeOID2BLLjiXiVutizbs+gDuxO837wBOutRTUO7GMjxKxkjKG92KDHvAab7CFt9j/2ZpPep8zHHJZNOFalWzUFr1l3ReVdK1nnHKZp1tsyJqrvxOzVi9uiP39erdzHM50QFVs0L7YmnlM4yWOhMVJfBIbEnoQNjnS++iKP0ZveSX2KqUnyAqhA3SQDhzHjmwcj97lCFdx22WF6cIB+cVr/iJvA5JEbWVag8AAAAAASUVORK5CYII=>

[image2]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACwAAAAZCAYAAABKM8wfAAACN0lEQVR4Xu2WPUhWYRTHT2iQ+FFCaKKgNihh4OAgOLg5hCASDaIObkbpklDUXEODS6GDBuEgggQNIugSSqDo5ODH5NAQ0mBOtln9/5zn6X3u4b7ve6+8QsP9wY/rPc/9OJ77POd5RTLy0g9n4TvYbcb+O+7Bcfd3OVyCjbnhf3DsGayyA2ngzQ3wuonb80K0w+ng/AXsCs49fNdrd0wNb/wO/8Cf8Bd8Ditgs2jV0lDmjtfgR9gajHkulTATegu/wTF408Vr4CJch8ew1sXT0gF/iCZuSZUwq7YvWlFWMh+vRK9JC6fQe9Ei+GR57ISPnKPwszv6WE9wfYQV0USWpfD8fAhPbTABE6IJ+JczUUuqCjPZM3jfDhiY8IYNOm7AWzYomuS85Kr2BNZHrlASJ8ybmTDbDVtLIdhDe20Q7MBDuCc6z2dgkxtjNfn80Eo3FpI4YTbzE3jXDiRgAF5IdBrxKx3A20EsCfwSLF7snPXwM67Cr7DajBWjTnSh/jbxB6Ktq+CLLwvLv+Es9ikew6fB+ZDo52U1Q97AERMrKR+keMLsx2sS3aF4HxPm0eMLELeTlQxW6kjiV66HvZkLKVyUC6IJ+98JhOuA64EbC3dDTo8rgS3tHA5LbhslXM0v4Z0g5ukTXXD8bcD5ygXI5/Cf4Pzm2ojbgktCG9yVXD+eg19Et9HJ4LoQJsmeyo1kE34S7RBbcBtOuWuuDD68BQ6KNnh+0rDa+bAbBu+J20AyMjIyMqL8BY/+XIFEtYV/AAAAAElFTkSuQmCC>

[image3]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADEAAAAZCAYAAACYY8ZHAAACl0lEQVR4Xu2XT4hOURjGH/k3/s/IghBJoZmFSUjZsZHIghA7hJpMTVNTSk1pNprFJEkiJAsSShbKQpGUjURWapKIsqAolPE83nvmvvc48/lu30zNV99Tv/ruec8997z/zr0f0FDNWkBOkPOkM7LVjR7DHJE2kB5n8zqKfN6400eyIvutTV5xNq+TKOHEKnKWPCVvyUXSXJhR1BzYgzX3OTlDlvoJ/9F093sN6XXXXqWcmEd2wOpziHyGOZbSBNJNfsLm7iRbyQw/qUpprQGyJDZkKuVE0HFyDba5bZEtSM24i3wj7yJbGa0jt8l8N7YcFpTALXLIXW8ik4dnJ9RE7pLTMCdSzabIyYHDsDn3iuaqpcirBOdm1725qaDSmVgE64ku2AYvFM1/pehNJZcwsqOxUr31gByERXcP6Siah1Xaic2w9KqMQpSVnaCZ5DKsf16RX2Sjs3tNJHvJe/ISdgio387B1tH6ni122z8q5YQ2KwfkyDLyAVbvyo7UCsvMNOSZukkmZXYvNarsR9xYCIwyqJKsVgpYxR7w0sYfkYXII63G1fGnjcsBOSJp89qQ+iKlH2QQtlZQHyrfMyraTm7AIqujUjWrhyqCerB/+CD5Tta6Ma9UxB8iD8qYSSeSyiRINazNaOwqrI6DfsMypYzFCvW+LxpXeT4jLdH4qGkWuY9ik+rU0Wb6Sbsbl1KRDpJjqSxVuqdm6RS5Tt6QNuQPUYMr4iqrINlWw2pe9pE2pFNrf/Z7NuzlWekLoCZpUS2uKAX0dRlsr2HvhSnkTjQvkDoad5NP5Ct5QY7BDg1lvG6l/lDP1Y0Ww0o0SCWnXtDpVxfSnxv1y0rYS/IA+UKe+EnjXYr6etj3lziVXfvMNNRQQ2OoP2F1ikX1riaSAAAAAElFTkSuQmCC>

[image4]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB0AAAAZCAYAAADNAiUZAAABNklEQVR4XmNgGAWUAxt0AXoAX3QBXCAEiJcC8Sw07AmV5wHiejQ5V6gcOiDaUl0GhMXPgDgDiAOAWAQqzwnEaUD8DYpBamFy6IBoS0GAF4gPA3E6mrgCEB8A4jggZkSRgQA+BogjYLgTjQ9yPE6gCcRvgdgYTfwkEJuiieEDJPk0CIj/MyCCjRWIy4BYAq6COECSpZMYIJaCglAIiNcC8S4UFcQBoi2FBS0okRwAYnMgPsGAcAQpgBtdABeABS0IwzTlQPmKMEXUBrCgXYQkBrLsCRBHIIlRDQgC8WkGiKXRSOKgYJ3CAAluUOFAVQCLz09ArI8mZwnE/4DYA02cYgBKpSBfrgNiLjQ5ULYByX0FYkcgZkaVJh2AfPGTAZGAYJgDKg8LAWS5R0AsB5UfBaNgFAxDAACFMT0RkS8eMwAAAABJRU5ErkJggg==>

[image5]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAC8AAAAZCAYAAAChBHccAAACTElEQVR4Xu2WPWgVQRSFj2BA0SiCYJGACVYGjIKICLFLk0KREFDQUjCgVSAIYpFCCxEbERGbhIDYpPMHUQiBNOmsRAuLEIJWaQMqas7h7ujMzeS9HVDIwjvwFW/P7s6d2TN3HtDRf9Nu0u0vNkXnKhqpouJHyBJZIYtkMLVxlsyRpxEnkjuAI2S28mImyS7yOONdJzv1sFNR8cfIGHlCfpPnSF/aQy6TefKTXCAHI1/Sb12/SX6Q+7B36t1S7P1y3r7qd+BeRfidGy/RAfIa9gU0ganEBXaQadgkWukW7Pncij6Eec+84VS08tJRWPE3YAN8JIciP0xO920lxeMV7HmvvWQB5l1LrU0qLl4rqpXpJ6uwQS5F/knyDjaJrdQLe1bR8NKk18g6OeU8r+LiVbgmoHg8ghX/BtZzpTC5VhqG5XnZXZfOw975AW3yi8LiQ2TCqqpgFa7BNJHSyMwg3YDic+W1i0yxcquqyGgwxUCReYnWp14cmSHnSXUjU6wQmVjarNq0mkBucl5xZNRavepGpkiKxFty3BuwdqlBX5DR1Nqku7B7dZjl2mRokdpTOR0mD8geb7SSz3us0Hn0yevmXQeRl9pku7xPw1qp7q2lLvIedqzvd17QGbSPzFVYcV/JgPM0xjhsw/al1h/1kdsoKF5FfYMNGphI7jCp86jN5aQJhxM5Rl9K2VbGvbeMdE9oclOwL7aAmsVvF2kvXUQDi1d7vQNb/cYVfxp/DzF1ok/kCvKtdlurcSsfpP/132Eb+gvswOuoo3+pDRHwkICUo+4FAAAAAElFTkSuQmCC>

[image6]: <data:image/png;base64,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>

[image7]: <data:image/png;base64,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>