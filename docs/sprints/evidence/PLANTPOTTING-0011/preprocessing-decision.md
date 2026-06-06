# PLANTPOTTING-0011 Phase 2 — preprocessing decision log

Behind the `ImagePreprocessor` / `OnDevicePlantIdentifier` seam. Each lever defaults to current
behaviour; kept only where the scorecard (`accuracy-eval.csv`, local Pixel_6_API_34, **41 fixtures /
30 species, 492 rows per mode**) measurably supports it. Adopt/drop judged on **top-1**
(threshold-independent) and **confident-wrong** (headline metric).

## Control

`squash` — the historical non-aspect-preserving `ResizeOp(inputSize, inputSize)`. BEFORE (current
thresholds): top-1 **0.528**, top-3 0.713, confident-wrong **0.382**, abstain 0.089, 20 ms / 83 ms.

## Levers measured (ALL cut)

| lever | top-1 | confident-wrong | med-lat | worst-lat | decision |
|---|---|---|---|---|---|
| **squash** (control) | 0.528 | 0.382 | 20 ms | 83 ms | shipping |
| center_crop | 0.528 | 0.382 | 20 ms | 189 ms | **DROP** — identical to squash, zero benefit |
| **tta6** (centre + 4 corners + full-frame) | 0.530 | **0.226** | 125 ms | 539 ms | **ADOPT** |

- **center-crop → DROP.** Identical top-1 and confident-wrong vs squash. `preprocess_mode` stays `"squash"`.
- **TTA → ADOPT at 6 views.** Centre + 4 corners + a 6th **full-frame (no-crop)** view (principal request).
  **Confident-wrong 0.382 → 0.226** (~40% lower), top-1 flat, at ~6× latency (20 → 125 ms median; 539 ms
  worst) on a one-shot identify. center-crop ≡ squash, so TTA clears the "beat center-crop" hurdle.

## Orientation / EXIF

Evaluated via the **rotate** perturbation rows: rotate confident-wrong **squash 0.378 → tta6 0.232**.
The CC0/PD fixtures carry no EXIF orientation tag, so an EXIF lever can't be measured directly; **no
separate EXIF normalisation added** — revisit on-device if real captures show an orientation problem TTA
doesn't cover.

## Locked shipping pipeline (input to Phase 3)

`model_manifest.json`: `preprocess_mode: "squash"`, `tta: 6`. TTA already raises abstain (0.089 → 0.244)
— it abstains on cases it used to get confidently wrong — so Phase 3 starts from a higher baseline abstain
and a much lower confident-wrong.
