# PlantPotting — User Guide

End-user guide for the PlantPotting Android app: point it at a houseplant, get a
species identification, and read a potting-mix recommendation. For build/run/setup, see
[`docs/runbook.md`](runbook.md); for the project's overall state, see
[`docs/ROADMAP.md`](ROADMAP.md).

## What the app does

PlantPotting identifies a houseplant from a photo using an **on-device** TensorFlow Lite
model (no network — identification runs entirely on your phone) and surfaces a
recipe-driven potting-mix recommendation drawn from a bundled knowledge base.

## The flow, step by step

1. **Grant camera access.** On first launch you'll see a permission screen — tap
   **Grant camera access**. If you previously denied it permanently, the app routes you to
   Settings and recovers automatically when you return.
2. **Frame and capture.** On the camera preview, tap the shutter. A brief overlay shows
   while the model binds and runs.
3. **One of two outcomes — both expected and correct:**
   - **High-confidence path** → you land directly on the **Result screen** with the source
     badge **`on-device match`**. This happens when the model is confident about an
     in-vocabulary species (e.g. *Monstera deliciosa* or *Crassula ovata*).
   - **Low-confidence path** → you land on the **Low-confidence picker**. The model
     couldn't pick a single species above its threshold (the bundled model covers only a
     couple of the knowledge-base species verbatim, and synthetic/emulator camera scenes
     rarely clear the bar). Pick the species row that matches your plant to continue; the
     Result screen then shows the badge **`on-device match (low confidence)`**.
4. **Read the recommendation.** On the Result screen, tap **See potting mix** to open the
   **Recommendation screen**:
   - a named **archetype** (e.g. *Aroid Chunky*),
   - a short horticultural **rationale**, and
   - a **recipe** table whose proportions always sum to 100%.
5. **Retake.** Tap **Retake** (or **Try again** from the failure banner) to return to the
   camera and capture again.

## Reading the source badge

The badge never lies about *how* the species was identified:

| Badge | Meaning |
| --- | --- |
| `on-device match` | The on-device model identified the species with high confidence. |
| `on-device match (low confidence)` | You picked the species from the low-confidence picker; the model was unsure. |
| `stub identifier` | A development/test build using the deterministic stub identifier (always *Monstera deliciosa*). You won't see this in a normal debug run of the real model. |

## If capture fails

If something goes wrong (e.g. the camera can't bind), a persistent banner appears at the
bottom of the camera screen with a **Try again** action. Tap it to retry.

## Known limitations

- Only a couple of the 16 knowledge-base species are in the model's vocabulary verbatim,
  so the **low-confidence picker is common** — that's expected today, not a bug. Widening
  direct coverage is tracked as the **V1** milestone in [`docs/ROADMAP.md`](ROADMAP.md).
- Identification is best-effort on-device ML, not a botanical authority. The potting-mix
  recommendation is guidance, not a guarantee.
