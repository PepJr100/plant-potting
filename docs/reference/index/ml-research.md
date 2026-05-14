# ML Research

Seven sources covering the machine-learning techniques the app's classifier and recommendation engine should be built on: MobileNetV4 and the ECCV 2024 paper give the model-architecture options; the Nature EfficientNet-B0 paper provides a directly-analogous transfer-learning success story for plant tasks; the model-calibration paper and its companion explainers (arxiv + Towards Data Science) frame how to make the classifier's confidence numbers honest — the single most important UX failure mode the app can avoid is over-confident wrong identifications. The Cambridge paper on consumer potting-mix preferences anchors the *output* side: what the substrate-recommendation engine should actually surface to users.

---

### MobileNetV4 — Universal Models for the Mobile Ecosystem (arXiv 2404.10518v1, preprint)
- **Cache file:** `arxiv_org_html_2404_10518v1.html`
- **Source:** https://arxiv.org/html/2404.10518v1
- **Summary:** The MobileNetV4 arXiv preprint (Qin et al., Google). Introduces the Universal Inverted Bottleneck (UIB) block and Mobile MQA attention, with neural architecture search refinements that push mobile-image-classifier Pareto efficiency further than MobileNetV3. Reports ImageNet classification, COCO object detection, and an enhanced distillation recipe. The leading 2024-vintage backbone for the app's on-device plant classifier. Same paper as the ECCV 2024 camera-ready entry below — keep both filed for convenience but they describe a single line of work.
- **Key concepts:** MobileNetV4, MNv4, Universal Inverted Bottleneck (UIB), Mobile MQA attention, hardware-independent Pareto efficiency, NAS, ImageNet classification, COCO object detection, distillation recipe, mobile deployment
- **Notable data:**
  - Paper structure: Hardware-Independent Pareto Efficiency → Universal Inverted Bottlenecks → Mobile MQA → MNv4 Design → Results (ImageNet + COCO) → Distillation
  - Targets on-device deployment across the mobile hardware ecosystem
  - Universal Inverted Bottleneck (UIB) is the headline architectural contribution
- **See also:** MobileNetV4 ECCV 2024 camera-ready (this file — same paper, version of record); MobileNetV4 — Papers Explained (this file); LiteRT: The Universal Framework (`android-tech.md`); EfficientNet-B0 Apple Leaf Diseases (this file)

### Understanding Model Calibration — A Gentle Introduction and Visual Exploration of Calibration and the Expected Calibration Error (ECE) (arXiv 2501.19047v2)
- **Cache file:** `arxiv_org_html_2501_19047v2.html`
- **Source:** https://arxiv.org/html/2501.19047v2
- **Summary:** A tutorial-style arXiv paper (January 2025) on model calibration centred on the Expected Calibration Error (ECE). Sections: "What is Calibration?" → "(Confidence) Calibration" → "Evaluating Calibration via ECE" → "ECE Drawbacks (binning, only max-probability, low-ECE ≠ high accuracy)" → multi-class, class-wise, and human-uncertainty calibration. Foundational for the app's confidence-score design: a 70%-confident wrong substrate recommendation is more useful than a 99%-confident wrong one.
- **Key concepts:** model calibration, confidence calibration, Expected Calibration Error (ECE), reliability diagrams, binning approach, multi-class calibration, class-wise calibration, human uncertainty calibration, ECE drawbacks
- **Notable data:**
  - Headline metric covered: Expected Calibration Error (ECE)
  - Drawbacks of ECE enumerated: low ECE ≠ high accuracy, binning artefacts, only max-probability considered
  - Companion to the Towards Data Science article (likely same author / same source material)
- **See also:** Expected Calibration Error (ECE) — Towards Data Science (this file); Plant Identification App Reviews — Bottega del Sarto (`app-landscape.md`)

### MobileNetV4: Universal Models for the Mobile Ecosystem (ECCV 2024, peer-reviewed)
- **Cache file:** `www_ecva_net_papers_eccv_2024_papers_ECCV_papers_05647_pdf.pdf`
- **Source:** https://www.ecva.net/papers/eccv_2024/papers_ECCV/papers/05647.pdf
- **Summary:** The peer-reviewed ECCV 2024 camera-ready version of MobileNetV4 (Qin, Leichner, Delakis, Fornoni, Luo, Yang, Wang, Banbury, Ye, Akin, Aggarwal, Zhu, Moro, Howard — Google). Same paper as the arXiv preprint 2404.10518 above; this is the version of record. Introduces the Universal Inverted Bottleneck (UIB) block (unifying Inverted Bottleneck, ConvNext, FFN, and the novel ExtraDW variants), the Mobile MQA attention block with >39% inference speedup over MHSA, a two-stage NAS recipe, and a JFT-augmented distillation recipe. The MNv4-Hybrid-L delivers 87% ImageNet-1K accuracy at 3.8 ms on Pixel 8 EdgeTPU. The strongest candidate backbone for the app's on-device classifier.
- **Key concepts:** MobileNetV4 (MNv4), Universal Inverted Bottleneck (UIB), ExtraDW, Mobile MQA, Multi-Query Attention, hardware-independent Pareto efficiency, Roofline Model, Ridge Point, two-stage NAS (TuNAS), JFT distillation, ImageNet-1K, COCO object detection, Pixel 8 EdgeTPU, Samsung S23 GPU, Apple Neural Engine
- **Notable data:**
  - MNv4-Conv-S: 73.8% top-1, 3.8 M params, 0.2 G MACs, 2.4 ms on Pixel 6 CPU
  - MNv4-Conv-M: 79.9% top-1, 9.2 M params; 50% faster than MobileOne-S4 and FastViT-S12; +1.5% over MobileNetV2 at comparable latency
  - MNv4-Hybrid-L: 83.4% top-1 base, **87% with distillation**, at 3.8 ms on Pixel 8 EdgeTPU
  - Mobile MQA: >39% acceleration over MHSA on EdgeTPU + Samsung S23 GPU with only −0.03% accuracy loss; −25% params and MACs vs MHSA
  - Asymmetric spatial down-sampling (stride-2 3×3 DW on K/V): +20% efficiency at −0.06% accuracy
  - Distillation recipe mixes D1 (RandAugment), D2 (Extreme Mixup), D3 (JFT-300M class-balanced, 130 K images/class) in a 1:1:2 ratio; +1.8% over Patient Teacher SOTA at 2000 epochs
  - COCO-17: MNv4-Conv-M detector reaches 32.6 AP, 23% faster than MobileNetV2 on Pixel 6 CPU; MNv4-Hybrid-M reaches 34.0 AP
  - Code + weights: github.com/tensorflow/models, plus timm: huggingface.co/collections/timm/mobilenetv4-pretrained-weights-6669c22cda4db4244def9637
  - Used in MLCommons MLPerf Mobile v4.0 inference benchmark
- **See also:** MobileNetV4 — arXiv 2404.10518 (this file — same paper, preprint version); MobileNetV4 — Papers Explained (this file); LiteRT: The Universal Framework (`android-tech.md`); EfficientNet-B0 Apple Leaf Diseases (this file); PlantCLEF2025 Kaggle Competition (`datasets.md`)

### A Fine-Tuned EfficientNet-B0 Convolutional Neural Network for Accurate and Efficient Classification of Apple Leaf Diseases (Nature Scientific Reports)
- **Cache file:** `www_nature_com_articles_s41598-025-04479-2.html`
- **Source:** https://www.nature.com/articles/s41598-025-04479-2
- **Summary:** A 2025 peer-reviewed Scientific Reports paper demonstrating fine-tuning of EfficientNet-B0 for apple leaf disease classification. The methodological template the app's classifier should follow: start from an ImageNet-pretrained efficient backbone, fine-tune on the domain-specific dataset, optimise for the accuracy/compute trade-off. Apple-leaf-disease and houseplant-identification are different downstream tasks but the transfer-learning recipe is the same.
- **Key concepts:** EfficientNet-B0, fine-tuning, transfer learning, leaf disease classification, plant pathology, lightweight CNN, ImageNet pretraining, agricultural ML, mobile-deployable model
- **Notable data:**
  - Peer-reviewed in Scientific Reports (Nature Publishing Group)
  - Demonstrates EfficientNet-B0 (one of the most efficient small backbones) works well for plant-image tasks
  - Direct precedent for the app's "fine-tune a mobile-friendly backbone on plant data" approach
- **See also:** MobileNetV4 — arXiv 2404.10518 (this file); PlantCLEF2025 Kaggle Competition (`datasets.md`); LiteRT Overview (`android-tech.md`)

### Automated Real-Time Identification of Medicinal Plants Species in Natural Environment Using Deep Learning Models — A Case Study from Borneo Region (Malik, Ismail, Hussein & Yahya, *Plants* 11(15):1952, 2022)
- **Cache file:** `www_mdpi_com_2223-7747_11_15_1952.html`
- **Source:** https://www.mdpi.com/2223-7747/11/15/1952 · Direct PDF: https://www.mdpi.com/2223-7747/11/15/1952/pdf?version=1659083941
- **Summary:** MDPI *Plants* open-access paper applying deep learning to real-time plant identification in *natural environment conditions* (the very setting the app needs to handle) — outdoors, mixed background, variable lighting, in Borneo. Methodologically the closest analog in the index to what the app is actually doing: a deep-learning species classifier deployed for in-situ photography, not curated database photos. Reports concrete accuracy numbers across multiple backbone architectures on a tropical-medicinal-plant dataset.
- **Key concepts:** deep learning, real-time identification, medicinal plants, species identification, natural-environment imagery, on-device deployment, transfer learning, tropical flora, Borneo, multi-architecture comparison
- **Notable data:**
  - Published 2022 in *Plants* (MDPI), Volume 11, Issue 15, Article 1952
  - Authors at Universiti Brunei Darussalam
  - Focus on "real-time" identification — implicitly mobile/edge deployment, same constraints as the app
  - Subject tags: deep learning, medicinal plants, species identification
- **See also:** MobileNetV4 ECCV 2024 (this file); EfficientNet-B0 Apple Leaf Diseases (this file); PlantCLEF 2025 — Martellucci et al BISS abstract (`datasets.md`); LiteRT: The Universal Framework (`android-tech.md`); Compose Native CameraX in 2026 (`android-tech.md`); Plant Identification Apps Tested — GrowItBuildIt (`app-landscape.md`)

### Exploring Consumer Preferences for Potting Mix Characteristics Using Best-Worst Scaling (Cambridge, Journal of Agricultural and Applied Economics)
- **Cache file:** `www_cambridge_org_core_journals_journal-of-agricultural-and-applied-economics_article_exploring-consumer-preferences-for.html`
- **Source:** https://www.cambridge.org/core/journals/journal-of-agricultural-and-applied-economics/article/exploring-consumer-preferences-for-potting-mix-characteristics-using-bestworst-scaling/2791546D07CFD9E794D58FEFFD3F990E
- **Summary:** Peer-reviewed economics paper using Best-Worst Scaling (a discrete-choice technique) to quantify what consumers actually value in a potting mix. Covers attribute rankings — ingredients, sustainability claims, price, branding, etc. — derived from survey data. The single most directly relevant academic paper for the app's recommendation UX: it tells you which substrate attributes to surface to users (and which to bury) based on real preference data, not designer intuition.
- **Key concepts:** consumer preferences, potting mix attributes, best-worst scaling, discrete choice experiment, willingness-to-pay, sustainability claims, peat-free preference, retail horticulture, choice modelling
- **Notable data:**
  - Published in *Journal of Agricultural and Applied Economics*, Volume 57 Issue 1
  - Method: Best-Worst Scaling — robust discrete-choice technique
  - Output: ranked importance of substrate attributes to consumers
- **See also:** Houseplant Trends 2025 — Happy Houseplants (`market-trends.md`); RHS Statement on Peat (`substrate-science.md`); Air Porosity and Water-Holding Ability — Sun Gro (`substrate-science.md`)

### Papers Explained 232: MobileNetV4 (ritvik19, Medium)
- **Cache file:** `ritvik19_medium_com_papers-explained-232-mobilenetv4-83a526887c30.html`
- **Source:** https://ritvik19.medium.com/papers-explained-232-mobilenetv4-83a526887c30
- **Summary:** Entry #232 in ritvik19's "Papers Explained" Medium series, summarising the MobileNetV4 paper for a developer audience. The Medium series is a popular practitioner-facing distillation of ML papers — useful as a fast on-ramp before reading the arXiv original, and for confirming the headline contributions a non-specialist would recognise.
- **Key concepts:** MobileNetV4, paper summary, Papers Explained series, UIB, Mobile MQA, mobile model architecture, NAS, plain-English deep learning, developer audience
- **Notable data:**
  - Article number 232 in the ritvik19 series
  - Practitioner-facing summary of the same material as arXiv 2404.10518
- **See also:** MobileNetV4 — arXiv 2404.10518 (this file); LiteRT: The Universal Framework (`android-tech.md`)

### Expected Calibration Error (ECE): A Step-by-Step Visual Explanation with Python Code (Towards Data Science)
- **Cache file:** `towardsdatascience_com_expected-calibration-error-ece-a-step-by-step-visual-explanation-with-python-code-c3e9aa12937d.html`
- **Source:** https://towardsdatascience.com/expected-calibration-error-ece-a-step-by-step-visual-explanation-with-python-code-c3e9aa12937d/
- **Summary:** Towards Data Science long-form walkthrough of Expected Calibration Error (ECE) with runnable Python code. Companion to the arXiv calibration paper above — the two cover the same material at different abstraction levels (paper = formal, TDS = code-along). For an engineering team integrating calibration into the app's confidence-output, this is the document the implementer should read first.
- **Key concepts:** Expected Calibration Error, ECE, calibration code, reliability diagrams, Python implementation, model confidence, post-hoc calibration, temperature scaling
- **Notable data:**
  - Step-by-step visual walkthrough with Python code
  - Pairs with the more formal arXiv companion (2501.19047v2)
- **See also:** Understanding Model Calibration — arXiv 2501.19047v2 (this file); Plant Identification App Reviews — Bottega del Sarto (`app-landscape.md`)
