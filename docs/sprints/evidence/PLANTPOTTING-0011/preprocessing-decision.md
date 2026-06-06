# PLANTPOTTING-0011 Phase 2 — preprocessing decision log

Behind the `ImagePreprocessor` / `OnDevicePlantIdentifier` seam. Each lever defaults to current
behaviour; kept only where the scorecard (`accuracy-eval.csv`, local Pixel_6_API_34, **25 fixtures /
14 species, 300 rows per mode**) measurably supports it. Adopt/drop judged on **top-1**
(threshold-independent) and **confident-wrong** (the headline metric).

## Control

`squash` — the historical non-aspect-preserving `ResizeOp(inputSize, inputSize)`. BEFORE (current
thresholds): top-1 **0.560**, top-3 0.733, confident-wrong **0.357**, abstain 0.083, 23 ms / 64 ms.

## Levers measured (ALL cut)

| lever | top-1 | confident-wrong | med-lat | worst-lat | decision |
|---|---|---|---|---|---|
| **squash** (control) | 0.560 | 0.357 | 23 ms | 64 ms | shipping |
| center_crop | 0.560 | 0.357 | 22 ms | 72 ms | **DROP** — identical to squash, zero benefit |
| **tta6** (centre + 4 corners + full-frame) | 0.577 | **0.197** | 144 ms | 300 ms | **ADOPT** |

- **center-crop → DROP.** Identical top-1 and confident-wrong vs squash; the model already tolerates the
  squash. `preprocess_mode` stays `"squash"`.
- **TTA → ADOPT at 6 views.** Centre + 4 corners + a 6th **full-frame (no-crop)** view (principal request,
  restores the plant's overall shape the zoomed crops lose). **Confident-wrong 0.357 → 0.197** (nearly
  halved), top-1 +1.7 pt, at ~6× latency (23 → 144 ms median; 300 ms worst) on a one-shot identify. The
  case rests on confident-wrong. N = 6. center-crop ≡ squash, so TTA clears the "beat center-crop" hurdle.

## Orientation / EXIF

Evaluated via the **rotate** perturbation rows, not a new lever. Rotation is the weakest axis and TTA
helps it: rotate confident-wrong **squash 0.380 → tta6 0.230**. The CC0/PD fixtures carry no EXIF
orientation tag, so an EXIF lever can't be measured directly; **no separate EXIF normalisation added** —
revisit on-device if real captures show an orientation problem TTA doesn't cover.

## Locked shipping pipeline (input to Phase 3)

`model_manifest.json`: `preprocess_mode: "squash"`, `tta: 6`. Phase 3 tunes thresholds against THIS
pipeline. TTA already raises abstain (0.083 → 0.227) — it abstains on cases it used to get confidently
wrong — so Phase 3 starts from a higher baseline abstain and a much lower confident-wrong.
