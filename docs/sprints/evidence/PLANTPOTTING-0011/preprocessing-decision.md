# PLANTPOTTING-0011 Phase 2 — preprocessing decision log

Behind the `ImagePreprocessor` / `OnDevicePlantIdentifier` seam. Each lever defaults to current
behaviour; kept only where the scorecard (`accuracy-eval.csv`, local Pixel_6_API_34, 20 fixtures /
14 species, 240 rows per mode) measurably supports it. Adopt/drop judged on **top-1**
(threshold-independent) and **confident-wrong** (the headline metric).

## Control

`squash` — the historical non-aspect-preserving `ResizeOp(inputSize, inputSize)`. BEFORE numbers
(current thresholds): top-1 **0.654**, top-3 0.796, confident-wrong **0.254**, abstain 0.092,
latency 30 ms median / 103 ms worst.

## Levers measured (ALL cut)

| lever | top-1 | confident-wrong | med-lat | worst-lat | decision |
|---|---|---|---|---|---|
| **squash** (control) | 0.654 | 0.254 | 30 ms | 103 ms | shipping |
| center_crop | 0.654 | 0.254 | 30 ms | 58 ms | **DROP** — identical to squash, zero benefit |
| **tta6** (centre + 4 corners + full-frame) | 0.667 | **0.113** | 184 ms | 453 ms | **ADOPT** |

- **center-crop → DROP.** Byte-for-identical top-1 and confident-wrong vs squash on this set; the
  model already tolerates the squash, so cropping to a square buys nothing. `preprocess_mode` stays
  `"squash"`.
- **TTA → ADOPT at 6 views.** Started as 5 crops (centre + 4 corners); a **6th full-frame (no-crop)**
  view was added (principal request) to restore the plant's overall shape the zoomed crops lose. TTA
  **more than halves confident-wrong (0.254 → 0.113)** and nudges top-1 (+1.3 pt), at ~6× latency
  (30 → 184 ms median; 453 ms worst). The case rests on confident-wrong — the exact failure this
  sprint targets — and the cost is acceptable on a one-shot "identify" tap. N = 6 (≤ the TTA budget).
  The plan's "beat center-crop" hurdle: center-crop ≡ squash here, and TTA beats both.

## Orientation / EXIF

Evaluated via the **rotate** perturbation rows rather than a new lever. Rotation is the model's
weakest axis, and TTA already improves it:

| rotate cut | top-1 | confident-wrong |
|---|---|---|
| squash | 0.625 | 0.288 |
| tta6 | 0.663 | 0.138 |

TTA's multi-view averaging recovers most of the rotation loss, so **no separate EXIF/orientation
normalisation is added this sprint** (the CC0/PD fixtures carry no EXIF orientation tag, so the
harness can't measure an EXIF lever directly; the rotate rows are the proxy). Revisit on-device if
real captures show an orientation problem TTA doesn't cover.

## Locked shipping pipeline (input to Phase 3)

`model_manifest.json`: `preprocess_mode: "squash"`, `tta: 6`. Phase 3 tunes thresholds against THIS
pipeline. Note TTA already raises the abstain rate (0.092 → 0.221) — it abstains on cases it used to
get confidently wrong — so Phase 3 starts from a higher baseline abstain and a much lower
confident-wrong.
