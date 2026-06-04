# PLANTPOTTING-0007 — conversion result (House Plant Species MobileNetV2 → TFLite)

Performed 2026-06-05 on the developer Windows machine (the offline step the agent sandbox
was originally blocked on). Steps 1–4 of `ACQUISITION.md` are complete; the bundle is
installed under `app/src/main/assets/ml/house_plant_species_mobilenetv2/`.

## Provenance
| Artifact | Value |
|---|---|
| Upstream `.h5` | `model/20240921-2014-full-image-set-mobilenetv2-Adam.h5` |
| Upstream `.h5` size | 22,554,800 bytes |
| Upstream `.h5` sha256 | `866f6251beb25cef1d02bf6143701a0a6ac39e3458f80ec098f08070d2f16f8f` |
| Converted `model.tflite` size | 10,923,936 bytes (~10.92 MB) |
| Converted `model.tflite` sha256 | `19ab94be1e77f878aa92a23450c4679f259523a67b83eef42412ef0b75d455ab` |
| Quantization | float16 (input/output tensors stay FLOAT32) |
| Input tensor | `[1,224,224,3]` float32 |
| Output tensor | `[1,47]` float32 → **label_count 47 == labels.csv** ✓ |

## Toolchain (offline; not added to the app)
Python 3.13.13 venv. TF 2.18 (the script's original target) does **not** support Python 3.13,
and modern TF defaults to Keras 3 which breaks `hub.KerasLayer` / `.h5` loading — so the
conversion used:
- `tensorflow==2.21.0`, `tf-keras==2.21.0`, `tensorflow-hub==0.16.1`, `pillow`, `numpy 2.1.3`
- `setuptools<81` (hub 0.16.1 still imports the removed `pkg_resources`)
- env `TF_USE_LEGACY_KERAS=1` so `tensorflow.keras` routes to tf-keras 2.x for the
  `hub.KerasLayer` custom-object deserialization.

Conversion was clean — standard MobileNetV2 + TF-Hub feature-vector ops, no unsupported-op or
deserialization blockers (the `ACQUISITION.md` "If conversion fails" path was not needed).

## Offline label-alignment sanity check
`sanity_infer.py` ran the converted TFLite over the 6 androidTest houseplant fixtures
(float32 + x/255, matching the manifest). This is the JVM/desktop-side cross-check; the
authoritative on-device numbers come from Phase 3 (`ModelSwapEvaluationTest`).

| Fixture | Expected | Top-1 | Result |
|---|---|---|---|
| dracaena-trifasciata.jpg | Snake plant | Snake plant (Sanseviera) — 82.1% | PASS |
| goeppertia-orbifolia.jpg | Calathea | Calathea — 100.0% | PASS |
| phalaenopsis.jpg | Orchid | Orchid — 100.0% | PASS |
| zamioculcas-zamiifolia.jpg | ZZ Plant | ZZ Plant (Zamioculcas zamiifolia) — 97.3% | PASS |
| spathiphyllum-wallisii.jpg | Peace lily | Daffodils 43.2% / **Peace lily 42.4%** (rank 2) | near-miss |
| epipremnum-aureum.jpg | Pothos | Chinese Money Plant (Pilea) — 99.9% | miss |

**4/6 top-1.** The four confident correct hits prove `labels.csv` ordering aligns with the
model's output indices (misalignment would yield random labels, not 80–100% correct ones) —
the alignment check the `label_count` guard cannot perform.

The two non-passes are genuine model behavior, not conversion artifacts, and are exactly what
the Phase 3 probe exists to quantify:
- **Peace lily** loses by 0.8 pt to Daffodils → a low-confidence/margin case under the
  unchanged AIY threshold policy.
- **Pothos** is confidently confused with Pilea (a real upstream-model weakness).

## Status against ACQUISITION.md
- [x] 1. Download upstream `.h5`
- [x] 2. Convert (float16)
- [x] 3. Finish `model_manifest.json` (placeholder→false, sha256, float32 input + normalization kept)
- [x] 4. Install bundle into `app/src/main/assets/ml/house_plant_species_mobilenetv2/`
- [ ] 5. **Phase 3 on-device probe** — run `ModelSwapEvaluationTest` on `pixel6Api34` emulator,
       paste `model-swap-eval.csv` (needs a running emulator)
- [ ] 6. Flip `ACTIVE_MODEL_ROOT` (Phase 5) — **only after** the probe confirms it beats AIY
- [ ] 7. Live smoke (`OnDeviceModelAppWiredPrototypeTest`) with the flag flipped

`ACTIVE_MODEL_ROOT` default deliberately **left at `ml/aiy_plants_v1`** — the swap harness
auto-discovers the new bundle without a flip; flipping the default is gated on Phase 3 evidence.
