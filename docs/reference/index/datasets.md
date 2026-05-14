# Datasets

These four sources cover the largest publicly available plant identification datasets and the authoritative botanical taxonomy reference. PlantCLEF 2025 (hosted on Kaggle, with the underlying paper archived on Internet Archive) is the de-facto benchmark for fine-grained plant identification under LifeCLEF / CVPR-FGVC. Plants of the World Online (POWO) from Kew Gardens is the most comprehensive open species-level taxonomic database and the lookup the app should use to normalise common names to accepted scientific names before piping species into the substrate recommendation engine.

---

### PlantCLEF 2025: Advancing AI-based Multi-Species Plant Identification in Vegetation Quadrats for Supporting Environmental Law and Biodiversity Monitoring (Martellucci, Goëau, Bonnet, Vinatier, Joly — BISS conference abstract)
- **Cache file:** `archive_org_details_plantclef2025ad9mart.html`
- **Source:** https://archive.org/details/plantclef2025ad9mart · DOI: https://doi.org/10.3897/biss.9.181733
- **Summary:** Peer-reviewed conference abstract published in *Biodiversity Information Science and Standards* 9: e181733 (published 15 Dec 2025), authored by the official PlantCLEF organisers: Giulio Martellucci (INRAE LISAH), Hervé Goëau and Pierre Bonnet (CIRAD AMAP), Fabrice Vinatier (INRAE), and Alexis Joly (INRIA LIRMM). Frames PlantCLEF 2025 as a benchmark for *forensic biodiversity* — directly motivated by EU Directive 2024/1203 (environmental crime) and Regulation 2023/1115 (imported deforestation), both of which require scalable plant-community inventory methods. The task is multi-label classification on high-resolution 50×50 cm quadrat images, with a strong train/test domain shift: training data is close-up single-plant citizen-science images; test data is overhead vegetation plots with many co-occurring species. Presented at Living Data 2025. There is a longer companion overview paper on arXiv (Martellucci et al. 2025, arXiv:2509.17602) for participants wanting the full methodology.
- **Key concepts:** PlantCLEF 2025, LifeCLEF, multi-label classification, vegetation quadrats (50×50 cm), forensic biodiversity, sample-averaged F1, EU Directive 2024/1203, EU Regulation 2023/1115, domain shift (citizen-science → quadrat), DINOv2 ViT, multi-scale tiling, INRAE / CIRAD / INRIA, Pl@ntNet, GUARDEN, MAMBO, Living Data 2025
- **Notable data:**
  - Training set: ~**1.4 million** close-up single-species images covering **~7,800 taxa** from Southwestern Europe (subset of Pl@ntNet)
  - Test set: ~**2,100** annotated high-resolution quadrat images (Pyrenean and Mediterranean floras)
  - Evaluation: sample-averaged F1 at the quadrat level
  - 2025 leaderboard: **best F1 = 0.35** without metadata vs. **0.29** in 2024
  - Participation: **500+ participants, 38 research teams, 659 distinct methods** submitted
  - Top methods overwhelmingly used **DINOv2 ViT base patch 14** (Goéau et al. 2024 PlantCLEF 2024 pretrained weights, on Zenodo: zenodo.org/records/10848263), often paired with multi-scale tiling
  - Key preprocessing insight: aligning compression settings and **Lanczos interpolation** between train and test made a measurable difference
  - Common failure mode noted: overfitting from over-tuning hyperparameters
  - Funding: EU GUARDEN (grant 101060693), EU MAMBO (grant 101060639), Pl@ntAgroEco (grant 22-PEAE-0009); compute via GENCI/IDRIS on Jean Zay (V100/A100/H100)
  - DOI: 10.3897/biss.9.181733 · CC-BY 4.0
- **See also:** PlantCLEF2025 Kaggle Competition (this file); PlantCLEF2025 Data Download (this file); Plants of the World Online (this file); MobileNetV4 ECCV 2024 (`ml-research.md`, alternative on-device backbone); EfficientNet-B0 Apple Leaf Diseases (`ml-research.md`, transfer-learning precedent)

### PlantCLEF2025 @ LifeCLEF & CVPR-FGVC (Kaggle Competition)
- **Cache file:** `www_kaggle_com_competitions_plantclef-2025.html`
- **Source:** https://www.kaggle.com/competitions/plantclef-2025 · Companion task page: https://www.imageclef.org/PlantCLEF2025
- **Summary:** Kaggle competition landing page for PlantCLEF 2025, co-hosted at LifeCLEF and CVPR-FGVC. The cached Kaggle HTML is an SPA so static content is thin, but the companion ImageCLEF task page documents the full schedule, rules, motivation, and dataset structure. Goal: predict all plant species visible in high-resolution vegetation-plot images (multi-label classification). The single largest public plant-identification benchmark in 2025 and the natural transfer-learning starting point for any plant-image model — especially via the released DINOv2 pretrained weights.
- **Key concepts:** Kaggle competition, LifeCLEF, CVPR-FGVC, multi-label species identification, vegetation quadrats, ecological monitoring, Pl@ntNet, citizen-science training, self-supervised / semi-supervised allowed, CEUR-WS working notes
- **Notable data:**
  - Kaggle competition ID **89850**; URL slug `plantclef-2025`
  - One-line scope: "Multi-species plant identification in vegetation quadrat images"
  - Schedule: registration opened Dec 2024 · **competition start 1 March 2025** · **deadline 19 May 2025** · working-notes papers 7 June 2025 · CLEF 2025 Madrid 9–12 Sept 2025
  - Rules: external data permitted *if* the team also submits an equivalent run with only supplied data; self-supervised and semi-supervised approaches encouraged; LUCAS-derived unlabeled cover images provided to enable SSL
  - Participation requires both CLEF registration (clef2025-labs-registration.dei.unipd.it) and a Kaggle account
  - Officially published ranking restricted to teams that submitted a CEUR-WS working-note paper
  - Funded by EU GUARDEN (101060693) and EU MAMBO (101060639)
- **See also:** PlantCLEF2025 Data Download (this file); PlantCLEF 2025 — Martellucci et al BISS abstract (this file); MobileNetV4 ECCV 2024 (`ml-research.md`); EfficientNet-B0 Apple Leaf Diseases (`ml-research.md`); EU AI Act Risk Classification Playbook (`regulatory.md`, downstream EU regulatory context)

### PlantCLEF2025 Data Download
- **Cache file:** `www_kaggle_com_competitions_plantclef-2025_data.html`
- **Source:** https://www.kaggle.com/competitions/plantclef-2025/data · Direct dataset mirrors: `lab.plantnet.org/LifeCLEF/PlantCLEF2024/single_plant_training_data/` and `lab.plantnet.org/LifeCLEF/PlantCLEF2025/`
- **Summary:** The Kaggle data tab for PlantCLEF 2025. The Kaggle page itself is an SPA, but the ImageCLEF companion page (above) enumerates every file in the corpus and provides direct download URLs at `lab.plantnet.org`, plus a Zenodo link to the pretrained DINOv2 weights. The training set is much larger than the test set (the train/test asymmetry — single plants → mixed quadrats — *is* the research challenge), and the pretrained DINOv2 ViT base patch 14 weights are the most directly useful artifact for any downstream plant-classification work that needs strong South-Western European flora coverage.
- **Key concepts:** dataset download, training set, test set, single-plant images, vegetation quadrat images, unlabeled cover images (LUCAS), DINOv2 ViT pretrained weights, GBIF metadata, taxonomic IDs
- **Notable data:**
  - **Single-plant training set** (subset of Pl@ntNet, SW Europe, 7.8k species, ~1.4M images):
    - Min side 800 px tar: ~281 GB
    - Max side 800 px tar: ~160 GB
    - Metadata CSV with `gbif_species_id` for cross-referencing GBIF
  - **Second training set** (LUCAS-derived unlabeled cover images, for self-supervised approaches): ~170 GB
  - **Vegetation-plot test set**: distributed via `lab.plantnet.org/LifeCLEF/PlantCLEF2025/vegetation_plot_test_data/PlantCLEF2025test.tar`
  - **Pretrained models**: ViT base patch 14 DINOv2 trained on the same SW-Europe Pl@ntNet flora — Zenodo records/10848263
  - Taxonomy caveat: species IDs may have shifted between PlantCLEF 2022/23 and PlantCLEF 2025 editions because Pl@ntNet, iNaturalist, and GBIF continually re-identify community images
- **See also:** PlantCLEF2025 Kaggle Competition (this file); PlantCLEF 2025 — Martellucci et al BISS abstract (this file); MobileNetV4 ECCV 2024 (`ml-research.md`); EfficientNet-B0 Apple Leaf Diseases (`ml-research.md`)

### Strengths and Challenges of Using iNaturalist in Plant Research with Focus on Data Quality (López-Guillén et al., *Diversity* 16(1):42, 2024)
- **Cache file:** `www_mdpi_com_1424-2818_16_1_42.html`
- **Source:** https://www.mdpi.com/1424-2818/16/1/42
- **Summary:** Open-access MDPI *Diversity* paper by López-Guillén, Herrera, Bensid, Gómez-Bellver, Ibáñez, Jiménez-Mejías, Mairal, Mena-García, Nualart, Utjés-Mascó and López-Pujol on what iNaturalist is actually good for in plant research and where the data falls down. iNaturalist is — alongside Pl@ntNet — the largest citizen-science plant-observation database in the world, and a key auxiliary data source for any plant-ID app (both for training data augmentation and for benchmarking against an alternative species-ID community). Critical reading for the app's data-quality story: iNaturalist observations are not equivalent to herbarium specimens, and the paper enumerates the failure modes.
- **Key concepts:** iNaturalist, citizen science, species discovery, threatened species, plant observation data, data quality, misidentification, Research Grade vs Casual, expert verification, biodiversity informatics, GBIF feed, sampling bias
- **Notable data:**
  - Published 2024 in *Diversity* (MDPI), Volume 16, Issue 1, Article 42
  - Authoring team based primarily at the Botanic Institute of Barcelona (IBB-CSIC)
  - DC subject tags include "citizen science", "species discovery", "threatened species"
  - Paired with PlantCLEF's organiser paper, gives the team both perspectives on community-contributed plant imagery: PlantCLEF is "Pl@ntNet's structured contest derivative", iNaturalist is "the wider unstructured pool"
- **See also:** PlantCLEF 2025 — Martellucci et al BISS abstract (this file); PlantCLEF2025 Kaggle Competition (this file); Plants of the World Online (this file); Plant Identification Apps Tested — GrowItBuildIt (`app-landscape.md`); Plant Identification App Reviews — Bottega del Sarto (`app-landscape.md`)

### Plants of the World Online (Kew Science)
- **Cache file:** `powo_science_kew_org.html`
- **Source:** https://powo.science.kew.org
- **Summary:** The Royal Botanic Gardens, Kew's open-access taxonomic database of every known plant species, integrating data from Kew's herbarium and global botanical literature. POWO is the gold-standard lookup for resolving common names to accepted scientific names, retrieving species synonyms, family membership, and global distribution. Critical for the app's species-normalisation layer: any plant the classifier returns should be reconciled to a POWO-accepted name before the substrate engine looks up requirements.
- **Key concepts:** botanical taxonomy, accepted scientific names, plant synonymy, species distribution, Kew Gardens, Plantae & Fungi, common-name resolution, plant family
- **Notable data:**
  - Authoritative source maintained by the Royal Botanic Gardens, Kew
  - Covers both Plantae and Fungi
  - Combines herbarium specimen records with literature-based descriptions
- **See also:** UConn Plant pH Preferences (`substrate-science.md`, which can be species-keyed against POWO); IPA Substrates for Orchids (`substrate-science.md`); plant profile entries (`plant-profiles.md`)
