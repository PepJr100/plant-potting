# Reference Index

A semantic index over the 72 reference documents cached in [`docs/reference/cache/`](../cache/). The source manifest is at [`docs/reference/urls.md`](../urls.md).

## Project context

This index supports an Android app project: an on-device ML system that lets a user photograph a houseplant, identify it, and recommend the correct potting substrate (with optional follow-on care guidance). The reference library was assembled to cover every dimension the build touches: the machine-learning research that justifies the model choices, the datasets the classifier will fine-tune on, the Android runtime stack the app deploys to, the market the app is being launched into, the competitor apps it will share shelf space with, the substrate science the recommendation engine must encode, the plant species the user is most likely to photograph, the pet-safety filter every recommendation must pass, and the regulatory regime (mostly the EU AI Act) the product must comply with.

Every document is filed in exactly one section file. The cross-topic index at the bottom of this README handles documents whose subject matter spans multiple sections.

## Sections

[**ML Research**](./ml-research.md) — *8 documents.* The machine-learning techniques the app's classifier and recommendation engine should be built on. Covers MobileNetV4 (the leading 2024 mobile backbone — both the arXiv preprint and the ECCV 2024 camera-ready, plus a Medium explainer), a Nature paper demonstrating EfficientNet-B0 fine-tuning for plant pathology, a Borneo-focused MDPI *Plants* paper applying deep learning to real-time medicinal-plant identification in natural environments (the closest methodological analog to the app), two model-calibration sources (the arXiv paper and its Towards Data Science companion), and a Cambridge economics paper on consumer potting-mix preferences using best-worst scaling.

[**Datasets**](./datasets.md) — *5 documents.* The largest publicly available plant-identification datasets, a data-quality assessment of the largest citizen-science alternative, and the authoritative botanical taxonomy reference. Three PlantCLEF 2025 sources (the Kaggle competition page, the data tab, and the organiser-authored BISS conference abstract by Martellucci, Goëau, Bonnet, Vinatier, and Joly — INRAE / CIRAD / INRIA), an MDPI *Diversity* paper on iNaturalist data quality (López-Guillén et al. 2024), plus Plants of the World Online (Kew) for canonical species names. The PlantCLEF training set covers ~7,800 SW-European taxa across ~1.4M citizen-science images, with DINOv2 ViT-base pretrained weights released on Zenodo — the most directly useful transfer-learning artifact for the app's classifier.

[**Android Tech**](./android-tech.md) — *5 documents.* The Android runtime stack the app is built on: three Google sources on LiteRT (formerly TFLite — the on-device inference runtime) and two CameraX sources (the official Jetpack configuration reference plus a Compose-first 2026 practitioner guide).

[**Market Trends**](./market-trends.md) — *10 documents.* The commercial opportunity around the app. Three AIPH sources sizing the global horticulture industry and one StatsMarketResearch report, plus six trend-focused articles from The Sill, Living Etc, Happy Houseplants, Love That Leaf, City Floral (Denver), and Terrarium Tribe on what consumers are buying and what's predicted for 2026.

[**App Landscape**](./app-landscape.md) — *6 documents.* The existing plant-identification app market. Three independent comparative reviews (GrowItBuildIt, Garden Myths, MyPlantIn), one head-to-head feature comparison (MyPlantIn vs PictureThis), one consumer-facing review aggregation, and one development-agency case study on building such an app.

[**Substrate Science**](./substrate-science.md) — *11 documents.* The scientific foundation of the recommendation engine. Foundational academic and extension-service references on substrate physics and chemistry (Raviv & Lieth, Purdue HO-255-W, U Arkansas Unit 7, Sun Gro, Greenhouse Management Chapter 12, plus a Sahin et al. 2002 paper measuring pH/EC/CEC/bulk-density/porosity across six pure substrates and fourteen binary mixes), a pH lookup table (UConn), orchid-specific guidance (IPA, Anthura on Phalaenopsis), and two RHS policy statements on peat.

[**Plant Profiles**](./plant-profiles.md) — *14 documents.* Species- and genus-level guides for the four plant categories that need the most nuance: aroids (including three DIY substrate recipes from Corisears, The Plant Puddle, and Hoya Treasures, plus the Foliage Factory aroid substrate guide and variegation explainer), orchids (Dark Orchid family-by-family breakdown, Harwoods bark guide), variegated specialty plants (three Monstera Albo guides, Philodendron Pink Princess, Variegated Monstera from Strange Wonderful Things), and carnivorous plants (California Carnivores, Costa Farms).

[**Pet Safety**](./pet-safety.md) — *4 documents.* The ASPCA Animal Poison Control Center's authoritative toxicity articles plus ASPCA Pet Insurance's consumer-facing list: the 2026 toxic-vs-non-toxic primer, the foundational mild/moderate/severe-tier guide, the deep dive on insoluble-calcium-oxalate plants (the most common toxicity-call category), and the ASPCA Pet Insurance dogs/cats/horses resource.

[**Regulatory**](./regulatory.md) — *9 documents.* The legal envelope. Six sources on the EU AI Act (the European Commission FAQ, three risk-classification explainers from GDPR Local, Trail-ML, and Glocert, plus Holland & Knight on the August 2026 deadline for US companies), three on AI liability under the revised 2024 Product Liability Directive (PrudAI, Bird & Bird's France-focused analysis, and White & Case's Germany-focused analysis), and one outlier on UK peat legislation (Jack Wallington) — substrate regulation that affects what the app can credibly recommend.

## Cross-topic index

Topics that surface across multiple sections, each pointing to the section files where the topic is treated.

**Aroids / Monstera / Philodendron / Pothos / Anthurium** — appears in [plant-profiles.md](./plant-profiles.md) (DIY recipes; Foliage Factory aroid guide; variegated Monstera and Pink Princess profiles), [substrate-science.md](./substrate-science.md) (UConn pH preferences; Sun Gro porosity), [pet-safety.md](./pet-safety.md) (all aroids are calcium-oxalate plants; ASPCA flags the genus).

**Best-Worst Scaling / consumer preferences** — [ml-research.md](./ml-research.md) (Cambridge paper), [market-trends.md](./market-trends.md) (consumer-facing 2025 trend reports).

**Calibration / ECE / overconfident predictions** — [ml-research.md](./ml-research.md) (arXiv 2501.19047 + Towards Data Science companion), [app-landscape.md](./app-landscape.md) (Bottega del Sarto on misidentification risk; Garden Myths' scepticism toward marketing claims).

**CameraX / camera capture** — [android-tech.md](./android-tech.md) (official configuration reference + 2026 Compose guide).

**Carnivorous plants (Venus flytrap, Nepenthes, Sarracenia)** — [plant-profiles.md](./plant-profiles.md) (California Carnivores; Costa Farms), [substrate-science.md](./substrate-science.md) (peat dependency in the RHS statements).

**Coir / coconut coir** — [substrate-science.md](./substrate-science.md) (Purdue HO-255-W; U Arkansas Unit 7; Sun Gro), [plant-profiles.md](./plant-profiles.md) (Anthura Phalaenopsis mixes mention coconut chips/fibre/grit).

**EU AI Act** — [regulatory.md](./regulatory.md) (Commission FAQ; GDPR Local; Glocert; Trail-ML; Holland & Knight on the August 2 2026 deadline), [ml-research.md](./ml-research.md) (calibration as the most direct technical control against "high-risk" classification errors), [app-landscape.md](./app-landscape.md) (every competing app — PictureThis, Plantin, PlantNet — faces the same regime).

**EfficientNet / MobileNet / on-device classifiers** — [ml-research.md](./ml-research.md) (MobileNetV4 arXiv + Papers Explained; EfficientNet-B0 apple-leaf-disease paper), [android-tech.md](./android-tech.md) (LiteRT is the runtime these run on).

**LiteRT / TFLite** — [android-tech.md](./android-tech.md) (three sources), [ml-research.md](./ml-research.md) (the deployment target for any backbone the team selects).

**MobileNetV4** — [ml-research.md](./ml-research.md) (arXiv 2404.10518; Papers Explained #232), [android-tech.md](./android-tech.md) (LiteRT deployment).

**Monstera albo variegata / Thai Constellation** — [plant-profiles.md](./plant-profiles.md) (three dedicated profiles plus Strange Wonderful Things on Borsigiana; Foliage Factory variegation explainer), [pet-safety.md](./pet-safety.md) (Monstera spp. flagged as a calcium-oxalate plant).

**Orchids / Phalaenopsis** — [substrate-science.md](./substrate-science.md) (IPA orchid substrates; Anthura Phalaenopsis advice; UConn pH 4.5–5.5), [plant-profiles.md](./plant-profiles.md) (Dark Orchid family-by-family; Harwoods bark guide), [pet-safety.md](./pet-safety.md) (orchids in the ASPCA pet-safe flower list).

**Peat / peat-free** — [substrate-science.md](./substrate-science.md) (RHS Statement on Peat; RHS Retail Peat-Free from 2026; sphagnum data in Purdue and U Arkansas), [regulatory.md](./regulatory.md) (Jack Wallington on UK peat-ban legislation), [market-trends.md](./market-trends.md) (Happy Houseplants on sustainability as a 2025 trend).

**Perlite** — [substrate-science.md](./substrate-science.md) (component data in Purdue HO-255-W, U Arkansas Unit 7, Sun Gro), [plant-profiles.md](./plant-profiles.md) (all three DIY aroid recipes; California Carnivores).

**Pet toxicity / calcium oxalates** — [pet-safety.md](./pet-safety.md) (all three ASPCA sources), [plant-profiles.md](./plant-profiles.md) (the aroid and Monstera profiles overlap with the calcium-oxalate-plant list).

**Philodendron Pink Princess** — [plant-profiles.md](./plant-profiles.md) (Gardenia profile; Foliage Factory variegation explainer; aroid substrate guide).

**PictureThis / PlantNet / PlantSnap / iNaturalist (competitor apps)** — [app-landscape.md](./app-landscape.md) (all six sources), [regulatory.md](./regulatory.md) (the EU AI Act applies to them too).

**PlantCLEF 2025** — [datasets.md](./datasets.md) (Kaggle competition + data tab + BISS conference abstract by the organisers), [ml-research.md](./ml-research.md) (the benchmark target for any plant-classification backbone).

**DINOv2 / pretrained ViT for plants** — [datasets.md](./datasets.md) (PlantCLEF 2025 released a DINOv2 ViT-base patch-14 fine-tuned on SW-European Pl@ntNet images, Zenodo records/10848263; the dominant 2025 leaderboard approach), [ml-research.md](./ml-research.md) (alternative backbones — MobileNetV4 for mobile, EfficientNet-B0 for the apple-leaf-disease transfer-learning precedent).

**EU environmental law (Directive 2024/1203, Regulation 2023/1115)** — [datasets.md](./datasets.md) (Martellucci et al. cite both as the policy motivation for PlantCLEF 2025's forensic-biodiversity framing), [regulatory.md](./regulatory.md) (broader EU regulatory regime the app sits inside).

**iNaturalist / Pl@ntNet / citizen-science plant data** — [datasets.md](./datasets.md) (iNaturalist data-quality paper; Pl@ntNet feeds PlantCLEF training data), [app-landscape.md](./app-landscape.md) (iNaturalist competes with PictureThis/Plantin/PlantNet in the consumer ID-app market), [ml-research.md](./ml-research.md) (community-contributed plant imagery is the dominant training-data source).

**Product Liability Directive 2024 / AI product liability** — [regulatory.md](./regulatory.md) (Bird & Bird France-focused, White & Case Germany-focused, PrudAI's general-counsel framing, Holland & Knight on the EU AI Act compliance deadline that interlocks with the PLD).

**Substrate measurement methodology (pH, EC, CEC, porosity, bulk density)** — [substrate-science.md](./substrate-science.md) (every entry in the section, but most quantitatively dense in the Sahin et al. 2002 Atatürk paper and the Greenhouse Management Chapter 12 / Sun Gro / Purdue references).

**Plants of the World Online (POWO) / botanical taxonomy** — [datasets.md](./datasets.md) (POWO entry), [substrate-science.md](./substrate-science.md) (UConn pH preferences are species-keyed).

**UK garden centres / UK horticulture** — [market-trends.md](./market-trends.md) (AIPH GCA growth report; Happy Houseplants UK trends), [substrate-science.md](./substrate-science.md) (RHS peat policies), [regulatory.md](./regulatory.md) (Jack Wallington on UK peat legislation).

**Variegation / variegated plants** — [plant-profiles.md](./plant-profiles.md) (Foliage Factory science guide; three Monstera Albo profiles; Philodendron Pink Princess; Variegated Monstera from Strange Wonderful Things).

---

_Total: 72 documents indexed across 9 section files. Source manifest: [`urls.md`](../urls.md). Last updated: 2026-05-13._
