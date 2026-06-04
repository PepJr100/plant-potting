# PLANTPOTTING-0007 — swap-evaluation summary (AIY vs House Plant Species MobileNetV2)

**Probe:** `ModelSwapEvaluationTest` on `pixel6Api34` (GMD), 2026-06-05. Raw per-fixture data:
`model-swap-eval.csv` (8 fixtures × 2 models = 16 rows). Both models fed each fixture through
their **own** manifest preprocessing (AIY UINT8 raw; candidate FLOAT32 `/255`).

## Headline

| Metric (over the 8 expanded fixtures) | `aiy_plants_v1` (baseline) | **`house_plant_species_mobilenetv2`** |
|---|---|---|
| top-1 correct **@ high-confidence** | **1** (monstera) | **6** (monstera, snake, calathea, orchid, ZZ, jade) |
| expected species in top-3 (mapped) | 2 | **8** |
| in-vocab fixtures (of 8 probed) | 2 | **8** |
| mean correct-class score | — (mostly out-of-vocab) | high on hits (1.00 ×4, 0.935, 0.5825) |
| median inference latency | 43 ms | **33 ms** (faster) |
| model size (`model.tflite`) | 5,056,146 B (4.82 MiB) | 10,923,936 B (10.42 MiB) |
| license | Apache-2.0 | Apache-2.0 |

**Winner: `house_plant_species_mobilenetv2`.** It is decisively better on the houseplant KB —
6× the high-confidence correct identifications, full top-3 coverage of all 8 probed species, and
*lower* latency — at a +5.87 MB asset cost that stays bundle-plausible (vs the disqualified ViT's
~85 MB). The AIY baseline anchor reproduced exactly (monstera 0.8984 high-conf; jade low-conf),
confirming no plumbing regression.

## Per-fixture (candidate)

| Fixture | Raw top-1 | Score | Mapped top-1 | Route | Read |
|---|---|---|---|---|---|
| monstera-deliciosa | Monstera Deliciosa | 1.0000 | monstera-deliciosa | high-conf | ✅ correct |
| dracaena-trifasciata (snake) | Snake plant (Sanseviera) | 1.0000 | dracaena-trifasciata | high-conf | ✅ correct |
| goeppertia-orbifolia | Calathea | 1.0000 | goeppertia-orbifolia | high-conf | ✅ correct (coarse map works) |
| phalaenopsis | Orchid | 1.0000 | phalaenopsis | high-conf | ✅ correct (coarse map works) |
| zamioculcas-zamiifolia (ZZ) | ZZ Plant | 0.9350 | zamioculcas-zamiifolia | high-conf | ✅ correct |
| crassula-ovata (jade) | Jade plant | 0.5825 | crassula-ovata | high-conf | ✅ correct (clears 0.55 barely; Money Tree close at 0.417) |
| spathiphyllum-wallisii (peace lily) | Peace lily | 0.4468 | spathiphyllum-wallisii | **low-conf** | ⚠️ correct top-1 but < 0.55; Boston Fern 0.306 behind → honest low-conf |
| epipremnum-aureum (pothos) | Chinese Money Plant (Pilea) | 0.9661 | crassula-ovata¹ | **low-conf** | ❌ confidently **wrong** (Pilea, unmapped) → routes low-conf; expected epipremnum present only as a weak rank-3 mapped candidate |

¹ pothos: raw top-3 are all unmapped (Pilea/Maranta/Anthurium); the mapped candidates come from
lower-ranked classes, so `mapped_top1` is a weak `crassula-ovata` — the verdict is **low-conf**,
which is the honest outcome for a confidently-wrong prediction. No threshold can or should
"fix" a wrong high-probability prediction.

## Selection-time criteria (size / latency — R3, R10)

- **Latency:** 33 ms median — *faster* than AIY (43 ms). Not a regression. ✅
- **Size:** 10.42 MiB raw. `noCompress "tflite"` is set, so the APK grows by ~the stored size.
  Both bundles ship (AIY stays the baseline asset), so total `ml/` assets ≈ 15.5 MiB. This is
  bundle-plausible and far under the ViT candidate's ~85 MiB that disqualified it. A future
  `--quant int8` build (~3–4 MB, UINT8 path) is the obvious size win if needed — recorded as a
  follow-up, not a blocker.

## Threshold decision (Phase 4 — re-baseline from this probe)

`per_species_thresholds` **stays `{}` (empty by design).** Deciding numbers:
- The 6 high-confidence hits clear the global 0.55 outright (4 at 1.00, ZZ 0.935, jade 0.5825) —
  no override needed.
- **peace lily @ 0.4468** is the only "correct-top-1-but-low-conf" case. It is **not** seeded: 0.4468
  is a sub-50% prediction with Boston Fern close behind (0.306, margin 0.14 < 0.18). Seeding an
  override ≤ 0.4468 to bless it would violate the 0006 anti-overfit discipline ("never lower
  thresholds to make a weak model look good"). It routes low-confidence to the picker — honest.
- **pothos** is a confidently-wrong prediction (Pilea), not a threshold case at all.

## License

Apache-2.0 (repo `LICENSE`). Weights-vs-dataset nuance recorded (we bundle the model, not the
community-collected training images). See `LICENSE-house-plant-species.txt` / `model-candidate-matrix.md`.

## Recommendation

Adopt `house_plant_species_mobilenetv2` as the on-device classifier behind the `ACTIVE_MODEL_ROOT`
switch. Per non-goal ("AIY stays the default `ACTIVE_MODEL_ROOT` target through close-out"), the
committed default remains `ml/aiy_plants_v1`; the prototype is demonstrated **flag-flipped** via
`OnDeviceModelAppWiredPrototypeTest` (G4 live run). Making it the shipped default is a one-line
follow-up decision. The 6 KB species still out-of-vocab (both Philodendrons, monstera-adansonii,
ficus-lyrata, chlorophytum, hoya) and the pothos→Pilea weakness are the agenda for a dedicated
fine-tuning sprint.
