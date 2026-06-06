# PlantPotting — User Guide

End-user guide for the PlantPotting Android app: point it at a houseplant, get a
species identification, and read a potting-mix recommendation. For build/run/setup, see
[`docs/runbook.md`](runbook.md); for the project's overall state, see
[`docs/ROADMAP.md`](ROADMAP.md).

## What the app does

PlantPotting identifies a houseplant from a photo using an **on-device** TensorFlow Lite
model (no network — identification runs entirely on your phone) and surfaces a
recipe-driven potting-mix recommendation drawn from a bundled knowledge base. It also keeps
a local **My Plants** collection and shows a license-clean reference photo for each plant.

## Home screen

The app opens on a **Home** screen with four tiles — **Identify new plant**, **My Plants**,
**Browse mixes**, **How it works** — plus a **recent plants** carousel of what you've saved.
A green **Home** button sits at the bottom of every screen to bring you back here.

## The flow, step by step

1. **Identify a new plant.** From Home, tap **Identify new plant**. On first launch you'll see
   a permission screen — tap **Grant camera access** (if you previously denied it permanently,
   the app routes you to Settings and recovers when you return).
2. **Frame and capture.** On the camera preview, tap the shutter. A brief overlay shows
   while the model binds and runs.
3. **One of three outcomes — all expected and correct:**
   - **High-confidence path** → you land on the **Result screen** with the badge
     **`on-device match`** and a numeric **confidence % + bar**. This happens when the model
     is confident about a mapped species.
   - **Low-confidence path** → you land on the **Low-confidence picker**. The model couldn't
     pick a single species above its threshold. The species list is contained within the
     search box — tap to search, then pick the row that matches your plant; the Result screen
     then shows **`on-device match (low confidence)`**.
   - **"Add this plant" path** → the model is strongly confident about a class with **no**
     knowledge-base entry. You see an **Add this plant** screen that locally tallies your
     request (so the maker knows what to add next), plus a **Pick manually** fall-through.
4. **Read the result + save.** The **Result screen** shows the plant's **reference photo**, the
   source badge, and the confidence. Tap **Save to My Plants** to keep it, or **See potting
   mix** to open the **Recommendation screen**: the plant name + picture, a horticultural
   **rationale**, the named **archetype** (e.g. *Aroid Chunky*), and a **recipe** whose
   proportions always sum to 100%.
5. **My Plants.** Open it from Home (or its Home tile). Saved plants show a thumbnail,
   confidence, and saved time; tap a row to revisit it, or the trash icon to remove it. The
   same plant can't be saved twice (it just refreshes).
6. **Browse mixes.** From Home, **Browse mixes** lists every substrate archetype as a card —
   tap one to see its recipe without identifying a plant first.

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

- The active model now maps **38 of its 47** classes to knowledge-base care cards (widened from
  10 → 26 by PLANTPOTTING-0009, then 26 → 38 by PLANTPOTTING-0010), but plants outside that
  vocabulary — and uncertain matches — still route to the **low-confidence picker**, so the
  picker remains common. That's expected today, not a bug. The mapped delta species are
  editorial coverage and have **not** yet been calibrated against real photos. Coverage progress
  is tracked as the **V1** milestone in [`docs/ROADMAP.md`](ROADMAP.md).
- Reference photos are **CC0 / public-domain** (Wikimedia Commons); a few are vintage botanical
  illustrations rather than photographs where that was the only license-clean option. Species
  without a clean image fall back to a placeholder.
- **My Plants** and the "Add this plant" tally are stored **locally only** (DataStore) — nothing
  syncs or leaves the phone.
- Identification is best-effort on-device ML, not a botanical authority. The potting-mix
  recommendation is guidance, not a guarantee.
