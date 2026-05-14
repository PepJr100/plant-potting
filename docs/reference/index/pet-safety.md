# Pet Safety

The ASPCA Animal Poison Control Center is the authoritative US source on plant toxicity to companion animals. These four sources cover the same domain at different levels of severity and audience: a quick-reference toxic-vs-non-toxic plant primer (flowers and houseplants), an older household-plant breakdown by toxicity severity, a deeper dive on insoluble-calcium-oxalate plants (the most common toxicity calls), and ASPCA Pet Insurance's species-keyed consumer-facing list. Pet safety is a critical filter the app must apply to every recommendation — the user's household pets effectively veto any plant that could harm them, and the substrate recommendation engine should never suggest a plant that lands a cat or dog in emergency care.

---

### Do You Know Which Flowers and Plants are Toxic to Pets? Our Experts Explain!
- **Cache file:** `www_aspca_org_news_do-you-know-which-flowers-and-plants-are-toxic-pets-our-experts-explain.html`
- **Source:** https://www.aspca.org/news/do-you-know-which-flowers-and-plants-are-toxic-pets-our-experts-explain
- **Summary:** Updated ASPCA (April 2026) toxicity primer covering the five flower/plant categories most associated with severe poisonings — lilies, sago palms, tulips, oleander, autumn crocus — alongside a parallel list of safe alternatives. Distinguishes between species that can cause life-threatening organ failure and those that merely cause GI upset. The clearest single source for the app's "high-risk plant" warning logic.
- **Key concepts:** lily toxicity (cats), sago palm, tulip bulbs, oleander cardiotoxicity, autumn crocus, calcium oxalates, pet-safe flowers, pet-safe houseplants, ASPCA Poison Control, kidney failure, liver failure
- **Notable data:**
  - Lilies (Lilium / Hemerocallis spp.) cause kidney failure in cats from pollen alone — potentially fatal
  - Sago Palm (Cycas revoluta): liver damage, bleeding disorders, seizures, death — seeds most toxic
  - Oleander causes cardiac arrhythmias and hypotension in dogs/cats/horses
  - Autumn Crocus (Colchicum autumnale): GI upset, arrhythmias, multi-organ failure, bone marrow suppression
  - Pet-safe flower list: Roses, Sunflowers, Zinnia, Gerbera, Snapdragons, Orchids, Asters, Statice, Freesia, Wax Flower
  - Pet-safe plant list: Petunia, African Violet, Easter Cactus, Hens and Chicks, Friendship Plant, Boston Fern, Spider Plant, Zebra Haworthia
  - ASPCA Animal Poison Control hotline: (888) 426-4435
- **See also:** Is That Houseplant Safe for Your Pets? (this file); These Houseplants Can Cause Trouble for Your Pets (this file); Plant pH Preferences — UConn (`substrate-science.md`, for the safe-plant species)

### Is That Houseplant Safe for Your Pets?
- **Cache file:** `www_aspca_org_news_houseplant-safe-your-pets.html`
- **Source:** https://www.aspca.org/news/houseplant-safe-your-pets
- **Summary:** Foundational ASPCA toxicity guide (originally Feb 2019) classifying common houseplants into mild, moderate, and severe toxicity tiers. Names exact genera and clinical signs for each. Less broad than the 2026 primer but tiered by clinical severity, which the app can map directly to UI warning levels.
- **Key concepts:** mild toxicity, moderate toxicity, severe toxicity, insoluble calcium oxalates, raphides, Fiddle Leaf Fig, Spider Plant, Pothos, Dieffenbachia, Peace Lily, Philodendron, Calla Lily, Aglaonema, Dracaena, Jade Plant, Sago Palm, Easter Lily
- **Notable data:**
  - Mild: Fiddle Leaf Fig (Ficus lyrata), Spider Plant (Chlorophytum comosum) → GI upset
  - Mild calcium-oxalate group: Pothos, Dumbcane, Peace Lily, Philodendron, Calla Lily, Chinese Evergreen → drooling, retching, vomiting, swelling
  - Moderate: Dracaena spp. (Corn Plant, Dragon Tree, Ribbon Plant) → vomiting, ataxia, mydriasis (cats)
  - Moderate: Jade Plant (Crassula ovata) → GI upset, tremors, elevated heart rate
  - Severe: Sago Palm → liver failure within 3 days; Easter Lily (cats) → kidney failure within 48–72 hours
- **See also:** These Houseplants Can Cause Trouble for Your Pets (this file); Do You Know Which Flowers and Plants are Toxic to Pets? (this file); Aroid Substrate Guide — Foliage Factory (`plant-profiles.md`, all aroids are calcium-oxalate plants)

### These Houseplants Can Cause Trouble for Your Pets
- **Cache file:** `www_aspca_org_news_these-houseplants-can-cause-trouble-your-pets.html`
- **Source:** https://www.aspca.org/news/these-houseplants-can-cause-trouble-your-pets
- **Summary:** Specialised ASPCA piece (October 2025) focused on insoluble-calcium-oxalate houseplants — the single most common toxic-plant category for which Poison Control receives calls. Explains the mechanism (needle-like raphide crystals), the symptom set, and at-home management. Critical because every popular aroid (Monstera, Pothos, Philodendron, ZZ, Alocasia) falls in this group.
- **Key concepts:** insoluble calcium oxalates, raphides, aroids, Dumb Cane, Calla Lily, Pothos, Devil's Ivy, Peace Lily, Chinese Evergreen, Elephant Ear, ZZ plant, Philodendron, Swiss Cheese Plant (Monstera), Anthurium, oral irritation, drooling, low-severity-but-frequent
- **Notable data:**
  - Ten genera flagged: Dieffenbachia, Zantedeschia, Epipremnum, Spathiphyllum, Aglaonema, Colocasia/Alocasia/Caladium, Zamioculcas, Philodendron, Monstera, Anthurium
  - Mechanism: raphide crystals released on chewing → oral pain, retching, vomiting, diarrhea
  - Severity is generally low; most cases manage at home with dairy (yogurt, milk, non-xylitol vanilla ice cream)
  - Severe (rare): throat swelling causing breathing/swallowing difficulty
- **See also:** Is That Houseplant Safe for Your Pets? (this file); Aroid Substrate Guide — Foliage Factory (`plant-profiles.md`); Monstera Albo and Philodendron Pink Princess entries (`plant-profiles.md`)

### Toxic and Non-Toxic Plants (ASPCA Pet Insurance)
- **Cache file:** `www_aspcapetinsurance_com_resources_plants-poisonous-to-cats.html`
- **Source:** https://www.aspcapetinsurance.com/resources/plants-poisonous-to-cats/
- **Summary:** ASPCA Pet Insurance's consumer-facing toxic-vs-non-toxic plant resource. Meta description: "Find out which plants are poisonous to dogs, cats and horses. Common toxic plants include sago palms, lilies, azaleas and tulips." Page-level description adds: "Our handy list contains plants that have been reported as having systemic effects on animals and/or intense effects on the gastrointestinal tract." Lower-friction consumer-style framing of the same data the three ASPCA News pieces cover with more clinical depth — useful for understanding how the toxicity information is normally presented to pet owners, and for cross-referencing the species names a pet owner will actually search for.
- **Key concepts:** consumer-facing toxicity list, dogs, cats, horses, sago palm, lilies, azaleas, tulips, systemic effects, gastrointestinal effects, ASPCA Pet Insurance, plant safety resource
- **Notable data:**
  - Insurance-product-tied resource (different audience than the ASPCA News pieces — pet owners shopping for or holding pet insurance)
  - Headline toxic plants flagged in the meta description: sago palms, lilies, azaleas, tulips
  - Covers three animal classes: dogs, cats, horses (the same triad as the ASPCA 2026 News primer)
- **See also:** Do You Know Which Flowers and Plants are Toxic to Pets? (this file); Is That Houseplant Safe for Your Pets? (this file); These Houseplants Can Cause Trouble for Your Pets (this file)
