# PLANTPOTTING-0007 — Results: Houseplant model swap (V1 entry)

**Outcome:** A houseplant-weighted on-device classifier was found, converted, evaluated, and
wired behind the `ACTIVE_MODEL_ROOT` switch. It **decisively beats** the AIY baseline on the KB
houseplants, and a live `pixel6Api34` run identifies real houseplant photos directly through the
production `OnDevicePlantIdentifier`. **Not a paper spike.**

## Selected model

**`house_plant_species_mobilenetv2`** — MobileNetV2 (TF-Hub feature vector + dense head), 47
house-plant classes, from [Vatsalyakrish02/House_plant_species](https://github.com/Vatsalyakrish02/House_plant_species)
(the Kaggle "House Plant Species" dataset). **Apache-2.0.**

- Converted out-of-band `.h5`→TFLite **float16**, `model.tflite` = 10,923,936 B, sha256
  `19ab94be1e77f878aa92a23450c4679f259523a67b83eef42412ef0b75d455ab`.
- Input `[1,224,224,3]` float32, `/255 → [0,1]` (app's FLOAT32 `ImagePreprocessor` branch,
  `NormalizeOp(0,255)`); output `[1,47]` float32. label_count 47 == labels.csv.
- Toolchain (offline, not in app): TF 2.21.0 + tf-keras 2.21.0 + tensorflow-hub 0.16.1,
  `TF_USE_LEGACY_KERAS=1`. Clean conversion, no op-fallback. (`CONVERSION-RESULT.md`.)

## Rejected candidates (survey + shortlist)

| Candidate | Why rejected |
|---|---|
| iNaturalist / Nature-Explorer plants | **Is** the AIY baseline (same ~2100-class iNat model) — not distinct |
| PlantNet-300K (MobileNetV3/EfficientNet INT8) | Those TFLite exports **don't exist**; only PyTorch ResNet published (~45 MB+, wild-flora) |
| PlantCLEF 2024 | ViT-base, ~85 MB — not bundle-fit; wild-flora |
| `dima806/house-plant-image-detection` | Same 47-class data, **89.97% acc**, Apache-2.0 — but ViT-base (85.8M params) **not bundle-fit**. Kept as the accuracy *ceiling* reference |
| `ademaulana/plantClassification` | No OSS license ("research only"); different (non-houseplant) dataset |
| `FilipK206/house_plant_classifier` | Model git-ignored (not committed); no license |

Full matrix: `docs/sprints/evidence/PLANTPOTTING-0007/model-candidate-matrix.md`.

## Fixture results (probe `pixel6Api34`, 2026-06-05)

`model-swap-eval.csv` (8 fixtures × {AIY, candidate}); summary `model-swap-eval-summary.md`.

| Fixture | AIY | Candidate |
|---|---|---|
| monstera-deliciosa | ✅ high-conf 0.8984 | ✅ high-conf 1.0000 |
| crassula-ovata (jade) | low-conf 0.1055 | ✅ high-conf 0.5825 |
| dracaena-trifasciata (snake) | low-conf (oov) | ✅ high-conf 1.0000 |
| zamioculcas-zamiifolia (ZZ) | low-conf (oov) | ✅ high-conf 0.9350 |
| goeppertia-orbifolia (calathea) | low-conf (oov) | ✅ high-conf 1.0000 (coarse map) |
| phalaenopsis (orchid) | low-conf (oov) | ✅ high-conf 1.0000 (coarse map) |
| spathiphyllum-wallisii (peace lily) | low-conf (oov) | ⚠️ low-conf 0.4468 (correct top-1, sub-threshold) |
| epipremnum-aureum (pothos) | low-conf (oov) | ❌ low-conf (confidently confused w/ Pilea) |

## Headline metrics vs AIY

| | AIY | Candidate |
|---|---|---|
| top-1 correct @ high-conf | **1** | **6** |
| expected in top-3 (mapped) | 2 | **8** |
| in-vocab fixtures (of 8) | 2 | **8** |
| KB-vocabulary coverage | **2/16** | **10/16** (8 exact + 2 coarse) |
| median inference latency | 43 ms | **33 ms** |

## Coverage delta & mapping

2/16 → **10/16**. New: epipremnum-aureum, spathiphyllum-wallisii, ficus-elastica,
dracaena-trifasciata, zamioculcas-zamiifolia, saintpaulia-ionantha (exact) + phalaenopsis,
goeppertia-orbifolia (coarse `Orchid`/`Calathea`). Still out-of-vocab (→ fine-tuning sprint):
monstera-adansonii, both Philodendrons, ficus-lyrata, chlorophytum-comosum, hoya-carnosa.
Aliases re-derived from the winner's *own* (common-name) labels — `docs/kb/ml-mapping-notes.md`.

## Threshold changes

**None.** `per_species_thresholds` ships `{}` (empty by design). The 6 hits clear the global 0.55
outright; peace lily @ 0.4468 is **not** seeded (sub-50%, Boston Fern 0.306 close behind — the
0006 anti-overfit discipline); pothos is confidently wrong, not a threshold case. Routing honesty
preserved.

## Seam

`PlantIdentifier` / `IdentificationResult` **unchanged**. The candidate's `[1,47]` FLOAT32 output
fits the existing `ModelScoreMapper`; FLOAT32 input fits the existing `ImagePreprocessor` branch.
No seam change, no new `PlantIdentifier` implementation — one `ACTIVE_MODEL_ROOT` switch.

## Live-prototype status (G4)

`OnDeviceModelAppWiredPrototypeTest` runs the production `OnDevicePlantIdentifier` with
`ACTIVE_MODEL_ROOT` pointed at the candidate (Hilt `@UninstallModules`+`@BindValue` — the
"flag-flipped" prototype) on `pixel6Api34`. It asserts monstera/snake/ZZ/jade identify
**high-confidence with the correct KB species id** through the production path (≥4, exceeding the
G4 ≥3 floor), and peace lily routes low-confidence. Evidence: GMD logcat under
`app/build/outputs/androidTest-results/managedDevice/`.

At sprint close the committed default was `ml/aiy_plants_v1` (non-goal: "AIY stays the default
target through close-out") and the candidate was demonstrated flag-flipped.

> **Post-sprint ship decision (2026-06-05).** On the conclusive probe evidence, `ACTIVE_MODEL_ROOT`
> was flipped to **`ml/house_plant_species_mobilenetv2`** — the House Plant Species model is now the
> production default. The AIY bundle stays in assets as the regression anchor;
> `OnDeviceModelRealInterpreterTest` now pins `ACTIVE_MODEL_ROOT=ml/aiy_plants_v1` so the AIY
> baseline assertions stay valid, and `ActiveModelRootContractTest` pins the new default. All JVM +
> `pixel6Api34` instrumented gates re-run GREEN after the flip. To revert: point the
> `buildConfigField` default back to `ml/aiy_plants_v1`.

## Size report (R10)

- candidate `model.tflite` 10,923,936 B (10.42 MiB) vs AIY 5,056,146 B (4.82 MiB); +5.87 MiB.
- `noCompress "tflite"` set → stored, not compressed; APK grows by ~the raw size.
- **assembled debug APK = 44,092,414 B (42.05 MiB)** with both bundles shipping (AIY kept as the
  baseline asset). Bundle-plausible — ≪ the ViT ~85 MiB that disqualified PlantCLEF/dima806.
- Follow-up size win if needed: `--quant int8` (~3–4 MB, UINT8 path).

## Gate results

- `verifyNoNetworking` — GREEN (continuous through Phase 3 + close-out; conversion stays out of Gradle).
- `scripts/check-stub-isolation.sh` — GREEN.
- Focused JVM (manifest/label/score/threshold) + full `testDebugUnitTest` — GREEN.
- `ktlintCheck` — GREEN.
- Instrumented `pixel6Api34`: `OnDeviceModelRealInterpreterTest` (AIY anchor), `ModelSwapEvaluationTest`
  (candidate assertions), `OnDeviceModelAppWiredPrototypeTest` (G4 live) — GREEN (7 tests, 1
  intentionally skipped — the out-of-vocab case self-skips with no ficus-lyrata fixture).
- `scripts/integration-flow.ps1 -BuildOnly` — GREEN ("Integration manifest diff passed";
  expected build artifacts **unchanged**: AIY stays the default identifier, no integration-output
  change). The device-aware cold/warm modes need a persistent adb device; on-device verification is
  already covered by the green GMD instrumented suite above (GMD tears its own emulator down).

## Recommendation / follow-ups

1. **Ship decision:** flip `ACTIVE_MODEL_ROOT` default to the candidate (one line) to make the
   coverage win user-facing — the evidence supports it.
2. **Fine-tuning sprint:** close the 6 out-of-vocab KB species and the pothos→Pilea weakness;
   `dima806`'s 89.97% on this exact vocab shows the data supports a stronger (distilled, mobile)
   model. Reuses this sprint's fixtures, harness, and `ACTIVE_MODEL_ROOT` mechanism.
3. **Optional size:** `--quant int8` rebuild (~3–4 MB) if APK budget tightens.
