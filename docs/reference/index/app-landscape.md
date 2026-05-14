# App Landscape

Six sources mapping the existing plant-identification app market and what users complain about. Three independent comparative reviews (GrowItBuildIt, Garden Myths, the MyPlantIn blog), one head-to-head feature comparison (MyPlantIn vs PictureThis), one consumer-facing review aggregation, and one case study on building a plant-ID app from a development agency. Useful for competitive positioning: PictureThis, Plantin, PlantNet, iNaturalist, PlantSnap, and Picture Insect dominate the category, but they are general-purpose identification apps — none of them couple identification to a substrate-recommendation engine, which is the wedge the app is built around.

---

### Plant Identification Apps Tested (GrowItBuildIt)
- **Cache file:** `growitbuildit_com_plant-identification-apps-tested.html`
- **Source:** https://growitbuildit.com/plant-identification-apps-tested/
- **Summary:** Rigorous independent comparison of plant identification apps. The reviewer fed each app the same 234 plant images with known correct IDs and tabulated accuracy. The clearest single benchmark of relative accuracy in the consumer plant-ID app market, with the caveat that the test set is the reviewer's curated set and not a standardised benchmark.
- **Key concepts:** plant identification accuracy, controlled benchmark, 234-image test set, PictureThis, PlantNet, iNaturalist, Seek, PlantSnap, app comparison, accuracy table
- **Notable data:**
  - Stated methodology: 234 images with known IDs put through each app
  - Methodology: "I put each app through a test of 234 images with known identifications. It took forever, but now I will report the results and show you the data!"
  - Output is an accuracy table the app's marketing can position against
- **See also:** Best Plant ID Apps — Garden Myths (this file); Best Plant Identification Apps — MyPlantIn (this file); PlantCLEF2025 Kaggle Competition (`datasets.md`)

### The Best Plant ID App: We Tested 7 Different Ones (Garden Myths)
- **Cache file:** `www_gardenmyths_com_the-best-plant-id-app-we-tested-7-different-ones.html`
- **Source:** https://www.gardenmyths.com/the-best-plant-id-app-we-tested-7-different-ones/
- **Summary:** Garden Myths' (Robert Pavlis) hands-on test of seven plant identification apps. Garden Myths is one of the most-followed myth-busting horticulture blogs and applies its usual sceptical lens to ID-app marketing claims. Reasonable cross-check on the GrowItBuildIt accuracy numbers.
- **Key concepts:** plant ID apps, 7-app comparison, hands-on testing, Robert Pavlis, Garden Myths, free vs paid apps, app accuracy, false positives, common-name vs scientific-name
- **Notable data:**
  - Tests seven apps head-to-head
  - Garden Myths' editorial position is consistently sceptical, which makes the verdicts useful
- **See also:** Plant Identification Apps Tested — GrowItBuildIt (this file); Plant Identification App Reviews — Bottega del Sarto (this file)

### Best Plant Identification Apps (MyPlantIn Blog)
- **Cache file:** `myplantin_com_blog_best-plant-identification-apps.html`
- **Source:** https://myplantin.com/blog/best-plant-identification-apps
- **Summary:** MyPlantIn's own blog roundup of the best plant identification apps — written by a competitor, so positioned to be MyPlantIn-favourable but useful for understanding how Plantin frames its own value proposition vs. PictureThis, PlantNet, etc. Pairs with the head-to-head Plantin vs PictureThis piece (same source).
- **Key concepts:** Plantin, PictureThis, PlantNet, iNaturalist, plant ID app comparison, freemium models, identification accuracy, plant care reminders
- **Notable data:**
  - Promotional in tone, but useful for feature-mapping each competitor
  - Lists all major incumbents the new app will share shelf space with
- **See also:** Plantin vs PictureThis — MyPlantIn (this file); Plant Identification Apps Tested — GrowItBuildIt (this file)

### Plantin vs PictureThis (MyPlantIn Blog)
- **Cache file:** `myplantin_com_blog_plantin-vs-picturethis.html`
- **Source:** https://myplantin.com/blog/plantin-vs-picturethis
- **Summary:** Head-to-head comparison between MyPlantIn and PictureThis — published by Plantin itself. Highlights Plantin's features that beat PictureThis on price, identification breadth, care reminders, and disease diagnosis. Even with bias factored out, this is the cleanest single source for understanding which features the two dominant apps in the category compete on.
- **Key concepts:** PictureThis, MyPlantIn, head-to-head comparison, plant ID accuracy, care reminders, disease diagnosis, subscription pricing, freemium, plant care app
- **Notable data:**
  - Direct feature-by-feature comparison between the category leader (PictureThis) and a major challenger (Plantin)
  - Promotional bias toward Plantin; useful as positioning artefact rather than neutral evaluation
- **See also:** Best Plant Identification Apps — MyPlantIn (this file); Plant Identification Apps Tested — GrowItBuildIt (this file)

### Plant Identification App Reviews That Save You From Wrong IDs (Bottega del Sarto)
- **Cache file:** `www_shop_bottegadelsarto_com_feed_plant-identification-app-reviews-737785.html`
- **Source:** https://www.shop.bottegadelsarto.com/feed/plant-identification-app-reviews-737785
- **Summary:** Consumer-facing roundup of plant-ID app reviews focused on avoiding misidentification (the practical failure mode users hit hardest with foraging and toxic-plant identification). The meta-description on the cached page is incongruent with the title — the page appears to be a syndicated/scraped feed with mixed editorial pedigree — so treat as supplementary corroboration rather than primary evidence.
- **Key concepts:** plant identification reviews, misidentification, wrong IDs, app review aggregation, consumer reviews, false-positive risk
- **Notable data:**
  - Title focus: helping users "save themselves from wrong IDs"
  - Meta description does not match title; possible content-feed page
- **See also:** Plant Identification Apps Tested — GrowItBuildIt (this file); ASPCA Do You Know Which Flowers and Plants are Toxic to Pets? (`pet-safety.md`); Expected Calibration Error (`ml-research.md`)

### Global Plant Identification Mobile App Development (Skynet Technologies Case Study)
- **Cache file:** `www_skynettechnologies_com_case-study_global-plant-identification-mobile-app-development.html`
- **Source:** https://www.skynettechnologies.com/case-study/global-plant-identification-mobile-app-development
- **Summary:** Development agency case study describing how Skynet Technologies built a global plant identification mobile app for a client — covering scope, tech stack, ML model choices, and time-to-ship. Practitioner artefact rather than research, but useful for benchmarking what a third-party builder ships and for sanity-checking the proposed scope of the project.
- **Key concepts:** plant ID app development, mobile app case study, machine learning integration, image classification, cross-platform deployment, agency engagement
- **Notable data:**
  - Single client case study; surfaces tech-stack choices and scope decisions
  - Useful as a buy-vs-build comparator
- **See also:** Compose Native CameraX in 2026 (`android-tech.md`); LiteRT: The Universal Framework (`android-tech.md`); Plant Identification Apps Tested — GrowItBuildIt (this file)
