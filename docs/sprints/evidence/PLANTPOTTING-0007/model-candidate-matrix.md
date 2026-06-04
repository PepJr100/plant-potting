# PLANTPOTTING-0007 — Phase 1: Candidate survey & decision matrix

**Date:** 2026-06-04
**Author:** opus (claude-opus-4-8), sprint-execute
**Scope:** Survey on-device, network-free, bundle-friendly TFLite classifiers with
houseplant-weighted vocabularies. Score each on KB-vocabulary overlap (vs the 16 KB
species), model size, inference latency profile, conversion risk, and license.
**License is a reported axis, not a hard gate** (per plan Phase 1 / G1).

> **⚠️ SUPERSEDED — see the ADDENDUM at the bottom of this file (2026-06-04, post-survey).**
> A reviewer-surfaced candidate (`house_plant_species_mobilenetv2`, a houseplant-specific,
> Apache-2.0, MobileNetV2 model covering ~10/16 KB species) was **missed** by the initial
> sweep below because the first pass dismissed houseplant-specific GitHub repos as "hobby /
> not production-credible" without inspecting their committed model artifacts. That
> dismissal was wrong. The "no public winner" conclusion in this header **no longer holds**;
> the corrected outcome is **one viable candidate, pending an `.h5`→TFLite conversion**.
> The original survey text is retained below unedited for provenance.
>
> **Original (now-corrected) headline outcome:** **No public, redistributable, bundle-ready, houseplant-weighted
> TFLite classifier was found that clears the bar.** All serious public plant models are
> either (a) the AIY baseline itself, (b) PyTorch *research* checkpoints with wild-flora
> vocabularies that need nontrivial conversion and are far over the bundle-size budget, or
> (c) ViT-scale models that are not bundle-fit. The only houseplant-*specific* TFLite
> artifacts found are amateur Teachable-Machine / hobby CNNs unsuitable for production.
> **Zero new candidates clear the redistribution/size/conversion bar → this is the
> explicitly-provisioned "no public winner" survey outcome** (plan Phase 1 final bullet,
> R1, acceptance criterion 4). See "Shortlist & survey outcome" and "Recommendation".

---

## The 16 KB species (the overlap ruler)

From `app/src/main/assets/kb/species.json` (scientific names; AIY aliases in parens):

| # | KB id | Scientific name |
|---|---|---|
| 1 | `monstera-deliciosa` | Monstera deliciosa |
| 2 | `monstera-adansonii` | Monstera adansonii |
| 3 | `epipremnum-aureum` | Epipremnum aureum |
| 4 | `philodendron-hederaceum` | Philodendron hederaceum |
| 5 | `philodendron-pink-princess` | Philodendron erubescens 'Pink Princess' |
| 6 | `spathiphyllum-wallisii` | Spathiphyllum wallisii |
| 7 | `ficus-lyrata` | Ficus lyrata |
| 8 | `ficus-elastica` | Ficus elastica |
| 9 | `dracaena-trifasciata` | Dracaena trifasciata (≡ *Sansevieria trifasciata*) |
| 10 | `zamioculcas-zamiifolia` | Zamioculcas zamiifolia |
| 11 | `chlorophytum-comosum` | Chlorophytum comosum |
| 12 | `phalaenopsis` | Phalaenopsis (genus) |
| 13 | `goeppertia-orbifolia` | Goeppertia orbifolia (≡ *Calathea orbifolia*) |
| 14 | `crassula-ovata` | Crassula ovata |
| 15 | `saintpaulia-ionantha` | Saintpaulia ionantha |
| 16 | `hoya-carnosa` | Hoya carnosa |

These are almost all **cultivated tropical ornamentals** (houseplants). That matters: the
large public plant models are trained on **citizen-science photos of wild/field flora**,
where indoor ornamentals are sparse or absent.

---

## Decision matrix

| Candidate (id) | Source | Weights available? | Redistribution status | Format / conversion risk | Input / dtype | Labels | Size (approx) | Latency profile | KB overlap (of 16) | License | Verdict |
|---|---|---|---|---|---|---|---|---|---|---|---|
| **`aiy_plants_v1`** (baseline, frozen) | [Kaggle: google/aiy vision-classifier-plants-v1/3](https://www.kaggle.com/models/google/aiy/tfLite/vision-classifier-plants-v1/3) | ✅ already bundled | ✅ Apache-2.0, redistributable | ✅ already TFLite, UINT8 — **no conversion** | 224, UINT8 | 2101 sci-names (`labels.csv`) | **5.06 MB** (`model.tflite`) | known-good on `pixel6Api34` (prior sprints) | **2/16** (Monstera deliciosa, Crassula ovata in-vocab; only Monstera routes high-conf @0.8984; jade @0.1055 low-conf) | Apache-2.0 | **Control column.** Stays default `ACTIVE_MODEL_ROOT`. |
| **`inat_plants_tflite`** (Google "Nature Explorer" / iNaturalist plants) | [AIY Nature Explorer](https://aiyprojects.withgoogle.com/model/nature-explorer/) | ✅ | ✅ (Google AIY) | ✅ TFLite | ~224, UINT8 | ~2100 plants + labelmap | ~5 MB | same class as AIY | **≡ AIY baseline** | Apache-2.0 | **Not a distinct candidate.** Google's Nature Explorer "plants" model is the iNaturalist-trained ~2100-plant MobileNet — **the same model family already shipped as AIY Plants V1**. Collapses into the baseline. |
| **`plantnet_300k_mobilenet_v3_small_int8`** (plan's primary candidate) | [plantnet/PlantNet-300K](https://github.com/plantnet/PlantNet-300K) | ❌ **does not exist as a published artifact** | n/a | n/a — would require training a MobileNetV3-Small (PlantNet publishes **no** mobile export) | — | — | — | — | — | — | **Disqualified: not available.** PlantNet-300K publishes **only PyTorch ResNet checkpoints** (see next row). There is no published MobileNetV3-Small / INT8 TFLite. Producing one means **training a new architecture** — out of scope (non-goal: no training). |
| **`plantnet_300k_efficientnet_lite0_int8`** (plan's comparator) | same | ❌ does not exist | n/a | n/a — same as above | — | — | — | — | — | — | **Disqualified: not available** (same reason — no published EfficientNet-Lite export). |
| **PlantNet-300K ResNet18/50/101** (what *is* actually published) | [PlantNet-300K README](https://github.com/plantnet/PlantNet-300K/blob/main/README.md) → weights on [Pl@ntNet Seafile](https://lab.plantnet.org/seafile/d/01ab6658dad6447c95ae/); ResNet-101 also on [Zenodo 4513879](https://zenodo.org/records/4513879) | ✅ (PyTorch `.tar`) | code repo **BSD-2-Clause** ([LICENSE](https://github.com/plantnet/PlantNet-300K/blob/main/LICENSE)); **weights license not explicitly stated**; dataset is CC-BY ([Zenodo 4726653](https://zenodo.org/records/4726653)) | ❌ **HIGH** — PyTorch → ONNX → TF → TFLite + INT8 quant; risk of unsupported ops; **no TF toolchain available in this environment** (Python 3.13, TF not installed) | 256→224, FLOAT32 (PyTorch) | 1081 wild-flora species | ResNet18 ≈ **45 MB**; ResNet50/101 larger — **≫ 5 MB AIY budget** (R10) | unknown until converted; ResNet18 heavier than MobileNet | **Not independently verified** (species metadata is not in the code repo; ships with the dataset). Qualitatively **low**: PlantNet-300K mirrors the Pl@ntNet citizen-science app — **wild/field flora, heavily W-European**. Monstera deliciosa is *plausibly* present (naturalised, app-recognised); most KB indoor ornamentals (ZZ, peace lily, pothos, snake plant, Hoya) likely **absent** or below the per-species image threshold. | BSD-2-Clause (code) / CC-BY (data) | **Rejected: not bundle-ready.** Wrong format (PyTorch), wrong size (≈45 MB+), wrong vocabulary (wild flora), high conversion risk, no conversion toolchain here. Even a clean conversion is unlikely to beat AIY on *houseplant* coverage. |
| **`plantclef_mobile_int8`** (stretch) | [Zenodo 10848263](https://zenodo.org/records/10848263); [HF: dino-v2-reg4-plantclef2024](https://huggingface.co/vincent-espitalier/dino-v2-reg4-with-plantclef2024-weights) | ✅ (PyTorch ViT) | research weights; CC-BY-ish | ❌ **VERY HIGH** — ViT-base/patch14 DINOv2; large, conversion to bundle-fit INT8 TFLite impractical | 518/224, FLOAT32 | ~7800 SW-Europe flora | ViT-base ≈ **85M params** (~85 MB INT8, ~340 MB fp32) — **not bundle-fit** (R10) | multi-100ms+ ViT inference (R3) | wild flora of SW Europe; houseplant overlap **near-zero** | research / CC-BY | **Rejected: not bundle-ready** (ViT-scale, wild flora). Reference-only. |
| **Houseplant-specific TFLite (hobby)** (Teachable Machine / small Keras CNNs, e.g. various GitHub/HF repos) | [example](https://huggingface.co/ademaulana/plantClassification), [example](https://github.com/umangjpatel/Plant-Classifier) | ✅ small `.tflite` | varies / often unstated | ✅ already TFLite (small) | varies | tiny class sets, no provenance | small | fast | unknown; tiny, uncurated class sets; no documented overlap with the 16 KB species | unclear / hobby | **Rejected: not production-credible.** Amateur models trained on small ad-hoc datasets, no label provenance, no documented houseplant species coverage, unstated/unsuitable licensing. Not a defensible swap. |

---

## Per-axis notes

### Vocabulary overlap (the decisive axis)
- The KB is 16 **houseplants**. The only public plant models at production scale are trained
  on **wild/field flora** (iNaturalist → AIY; Pl@ntNet → PlantNet-300K & PlantCLEF). The
  premise the sprint set out to test — "swap AIY for a houseplant-weighted model" — runs into
  the fact that **no public, houseplant-weighted, production-grade TFLite classifier exists**.
- AIY's 2/16 is not a quirk of AIY; it is structural. PlantNet/PlantCLEF would very likely
  score **similarly or worse** on these 16 indoor ornamentals (they cover *more* wild species,
  not *more houseplants*), so even a successful (expensive) conversion is unlikely to clear the
  G4 bar of "identifies real houseplant fixtures." (R4: this is exactly why we do not select on
  Monstera + jade alone — but here the constraint binds at the *availability* stage, before any
  fixture probe.)

### Redistribution (dataset-public ≠ weights-redistributable — the trap the plan called out)
- **PlantNet-300K dataset** is CC-BY (Zenodo 4726653). The **code repo** is BSD-2-Clause. The
  **pretrained weights** carry **no explicit license statement** in the README — a real
  ambiguity. BSD-2-Clause plausibly extends to the repo's artifacts, but this is unconfirmed.
- **PlantCLEF** weights are research artifacts (CC-BY-ish) — not cleanly productizable.

### Size / latency (R3, R10 — selection-time disqualifiers)
- AIY ships at **5.06 MB** UINT8. ResNet18 (~45 MB) and ViT-base (~85 MB INT8) blow the
  bundle budget by **9×–17×** before any accuracy comparison. These are disqualified on size
  alone, independent of vocabulary.

### Conversion risk (R2 — "nominally available" ≠ "bundle-ready")
- The plan's named primaries (`mobilenet_v3_small_int8`, `efficientnet_lite0_int8`) **are not
  published**. PlantNet only ships **PyTorch ResNet** checkpoints. Converting any of these to
  bundle-fit INT8 TFLite needs a PyTorch→ONNX→TF→TFLite pipeline with INT8 calibration — and
  **the TF toolchain is not available in this environment** (Python 3.13, TF not installed).
  Conversion risk is HIGH and, for this sprint, **blocked**.

---

## Shortlist & survey outcome

**Candidates that clear the redistribution + size + conversion + bundle-ready bar: ZERO.**

- `aiy_plants_v1` — the frozen baseline / control (stays the default).
- `inat_plants_tflite` — **≡ AIY**, not a distinct option.
- `plantnet_300k_mobilenet_v3_small_int8` / `efficientnet_lite0_int8` — **do not exist** as
  published artifacts.
- PlantNet-300K ResNet (what's actually published) — wrong format, ~45 MB+, wild-flora vocab,
  HIGH conversion risk, no toolchain → **not bundle-ready**.
- `plantclef_mobile_int8` — ViT-scale, not bundle-fit → **reference-only**.
- Hobby houseplant TFLite — not production-credible.

Per the plan's Phase 1 final bullet and R1, **this records as the survey outcome: no viable
public swap.** Phases 2–3 proceed with whatever is probeable — **worst case AIY only** — to
(a) build the expanded fixture ruler and the swap-evaluation harness, and (b) demonstrate the
**swap *mechanism*** (the single `ACTIVE_MODEL_ROOT` switch) end-to-end, even though it points
back at AIY for now.

## Recommendation (feeds Phase 6 results doc)

1. **No public houseplant-weighted TFLite model is available to swap in.** The structural
   reason: production-scale public plant models are wild-flora classifiers (iNaturalist,
   Pl@ntNet), not houseplant classifiers. The KB's 16 species are indoor ornamentals that
   these datasets under-cover.
2. **Recommend a dedicated fine-tuning sprint** (deliberately a *separate* sprint — training is
   a non-goal here): fine-tune a MobileNetV3-Small / EfficientNet-Lite0 **KB-16 head** on
   licensed houseplant imagery, export INT8 TFLite (~few MB), and swap it in through the
   `ACTIVE_MODEL_ROOT` mechanism this sprint will have proven. This is the only path to a model
   that actually covers the 16 KB houseplants.
3. **This sprint's value, given the negative finding:** the expanded fixture ruler (Phase 2),
   the repeatable swap-evaluation harness (Phase 3), and the proven one-switch swap mechanism
   (Phase 5) — all reusable by the recommended fine-tuning sprint.

---

## ADDENDUM (2026-06-04, post-survey) — viable houseplant-specific candidate found

The initial sweep above **wrongly dismissed houseplant-specific GitHub repos** as "hobby /
not production-credible" without inspecting their committed artifacts. A review pass found a
genuine candidate that **changes the survey outcome from "no winner" to "one viable
candidate, pending conversion."**

### Candidate: `house_plant_species_mobilenetv2`

| Axis | Finding |
|---|---|
| Source | [Vatsalyakrish02/House_plant_species](https://github.com/Vatsalyakrish02/House_plant_species) (this is the trained model + the Kaggle "House Plant Species" 47-class dataset) |
| Artifact | **Committed model:** `model/20240921-2014-full-image-set-mobilenetv2-Adam.h5` — **22.5 MB**, Keras `.h5`, full-47-class training run (a 1000-image variant is also present) |
| Architecture | **MobileNetV2** backbone via a TF-Hub `hub.KerasLayer` feature-vector module + classification head (47 classes) |
| Input / preprocessing | **224×224**, RGB, normalized **/255 → [0,1]** (the TF-Hub convention). Maps cleanly onto the app's existing **FLOAT32** `ImagePreprocessor` branch (`NormalizeOp`), so no new preprocessing code path. |
| Labels | 47 hardcoded class names with scientific names in the folder/label strings (e.g. `Snake plant (Sanseviera)`, `ZZ Plant (Zamioculcas zamiifolia)`) — trivial to author `plant_class_map.json` from. |
| Format / conversion risk | `.h5` → TFLite. MobileNetV2 + TF-Hub feature vector are all standard TFLite-supported ops → **LOW–MEDIUM risk** (far easier than PlantNet's PyTorch-ResNet or PlantCLEF's ViT). Path: `load_model(..., custom_objects={'KerasLayer': hub.KerasLayer})` → `tf.lite.TFLiteConverter.from_keras_model`. INT8 needs a representative-image set (a handful of dataset images). **Blocked in *this* sandbox** (no TF; Python 3.13) — must be run once offline by the user, output checked into `app/src/main/assets/ml/<id>/` (the script lives in `scripts/` or `evidence/`, never a Gradle task → `verifyNoNetworking` stays GREEN). |
| Size (estimated post-conversion) | fp32 TFLite ≈ 9–14 MB; **fp16 ≈ ~7 MB; INT8 ≈ ~3.5 MB** (could be *smaller* than AIY's 5.06 MB). Must be confirmed by actually converting. |
| Latency | unmeasured; MobileNetV2 @224 is a standard fast mobile model — expected comparable to AIY (measure in Phase 3 harness). |
| Accuracy | **unverified** — single-developer training run, no reported metrics. The Phase 3 harness over the expanded fixtures is exactly how we'd measure it before selecting. It could underperform; that's measurable, not assumed. |
| License | Repo `LICENSE` file is **Apache-2.0** (GitHub license detection; full 11 KB text committed). README has an unfilled "NONE License" *template* line — cosmetic, not the actual license. **Weights-vs-dataset nuance:** the 47-class image set was community/Kaggle-collected; we bundle the **model weights** (Apache-2.0 repo artifact), not the images — record the nuance, same as any ImageNet-pretrained model. |

### KB overlap: **~10 of 16** (vs AIY's 2/16) — and it covers the *common* houseplants AIY misses

| KB species | 47-class match | Strength |
|---|---|---|
| monstera-deliciosa | Monstera Deliciosa (Monstera deliciosa) | ✅ exact |
| epipremnum-aureum | Pothos (Ivy arum) | ✅ (Pothos = Epipremnum aureum) |
| spathiphyllum-wallisii | Peace lily | ✅ (Spathiphyllum) |
| ficus-elastica | Rubber Plant (Ficus elastica) | ✅ exact |
| dracaena-trifasciata | Snake plant (Sanseviera) | ✅ (≡ Sansevieria/Dracaena trifasciata) |
| zamioculcas-zamiifolia | ZZ Plant (Zamioculcas zamiifolia) | ✅ exact |
| crassula-ovata | Jade plant (Crassula ovata) | ✅ exact |
| saintpaulia-ionantha | African Violet (Saintpaulia ionantha) | ✅ exact |
| phalaenopsis | Orchid | ⚠️ coarse (Orchid ⊃ Phalaenopsis) |
| goeppertia-orbifolia | Calathea | ⚠️ genus (Goeppertia split from Calathea); also "Rattlesnake Plant (Calathea lancifolia)" = different species |
| monstera-adansonii | — | ❌ absent (only deliciosa) |
| philodendron-hederaceum | — | ❌ absent |
| philodendron-pink-princess | — | ❌ absent |
| ficus-lyrata | — | ❌ absent (only elastica) |
| chlorophytum-comosum | — | ❌ absent (spider plant not in 47) |
| hoya-carnosa | — | ❌ absent |

**8 strong + 2 coarse/genus ≈ 10/16**, a 5× coverage jump over AIY, concentrated in the
high-traffic species (snake plant, ZZ, pothos, peace lily, rubber, jade, african violet).

### Equivalents to also consider (the user asked)
The underlying **Kaggle "House Plant Species" (47-class)** dataset is widely reused, so other
trained models exist on the same labels (HuggingFace image-classification models, other
GitHub repos — some possibly already in a non-`.h5` format, or PyTorch/timm). Worth a short
Phase-1b shortlist pass so the harness can compare ≥2 houseplant-specific candidates rather
than betting on one developer's training run. Net: at least **one** clears the bar; likely
**more** among same-dataset siblings.

### Corrected recommendation
1. **A viable houseplant-specific swap candidate exists** (`house_plant_species_mobilenetv2`,
   Apache-2.0, ~10/16, MobileNetV2/224/[0,1], bundle-plausible after conversion). The original
   "no public winner" finding is **withdrawn**.
2. **The one gating step** is an offline `.h5`→TFLite (ideally INT8) conversion the user runs
   (no TF toolchain in this sandbox), with the converted model + a representative-image
   calibration set checked in. Everything else (fixtures, harness, mapping, `ACTIVE_MODEL_ROOT`
   wiring, gates) is unchanged from the plan.
3. **Quality is unverified** — the Phase 3 harness over the expanded fixtures decides whether it
   actually beats AIY on the in-vocab species before it's selected (R4 discipline holds).
4. A **dedicated fine-tuning sprint** remains the path to covering the **6 missing** KB species
   (both Philodendrons, monstera-adansonii, ficus-lyrata, chlorophytum, hoya) — but it is no
   longer the *only* path to a better-than-AIY prototype.

## Sources
- House Plant Species (model + 47-class dataset, Apache-2.0): https://github.com/Vatsalyakrish02/House_plant_species
- AIY Plants V1: https://www.kaggle.com/models/google/aiy/tfLite/vision-classifier-plants-v1/3
- Google Nature Explorer (iNaturalist plants): https://aiyprojects.withgoogle.com/model/nature-explorer/
- PlantNet-300K repo & README: https://github.com/plantnet/PlantNet-300K — https://github.com/plantnet/PlantNet-300K/blob/main/README.md
- PlantNet-300K license (BSD-2-Clause): https://github.com/plantnet/PlantNet-300K/blob/main/LICENSE
- PlantNet-300K ResNet-101 weights (Zenodo): https://zenodo.org/records/4513879
- Pl@ntNet-300K dataset (CC-BY, Zenodo): https://zenodo.org/records/4726653
- PlantCLEF 2024 weights (Zenodo): https://zenodo.org/records/10848263 — HF: https://huggingface.co/vincent-espitalier/dino-v2-reg4-with-plantclef2024-weights
