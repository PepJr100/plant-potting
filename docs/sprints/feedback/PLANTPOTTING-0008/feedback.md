# PLANTPOTTING-0008 Feedback

**Review verdict:** the spike's deliverables are **clean** — no bugs, no UX issues. All 84 plan
checkboxes hold up; the data-availability report, GO/NO-GO matrix, and fine-tune outline are rigorous
and internally consistent. Cheap gates re-run GREEN at review (`ktlintCheck`, `testDebugUnitTest`
incl. `resetFromTerminalSuccessReturnsToIdle`, `verifyNoNetworking`, `check-stub-isolation.sh`); binary
/ KB / model / `ACTIVE_MODEL_ROOT` audit clean; shutter-on-return fix accepted (test-coverage-backed,
no emulator attached at review).

**What changed is the *direction*, not the deliverables.** Two pieces of forward guidance from the user
reshape the next sprint — captured below. The 0008 spike correctly reported what CC data exists; the
user's constraints change which of its recommended paths we take.

## Bugs

None.

## UX Issues

None. (Shutter-on-return fix reviewed and accepted.)

## Missing Features / Scope guidance for the next sprint

### NEW DIRECTION — the next sprint is a text-only KB-expansion over the model-covered delta

Surfaced during review: the production model (`house_plant_species_mobilenetv2`) has **47 classes** but
only **10** map to the KB (8 exact + 2 coarse). That leaves a **delta of 37 model classes the app can
already recognize but has no KB care entry for** — if the model lands on one today, there is nothing to
show. **Closing this delta is text-only** (`species.json` + `archetypes.json` + `plant_class_map.json`
rows): the model already predicts these classes, so it needs **no training, no fine-tune, and no
self-shot imagery**. This is far higher ROI per effort than the 0008 fine-tune path and aligns with the
user's no-self-shot constraint (below).

**User decision: the next sprint covers this.** Target = the top popular true houseplants in the delta
plus two the user owns. Excluding seasonal/florist/outdoor classes (Daffodil, Tulip, Hyacinth,
Lily-of-the-valley, Lilium, Chrysanthemum), the target set is **~17 species**:

| # | Model class | Botanical | Note |
|---|---|---|---|
| 1 | Aloe Vera | *Aloe vera* | ubiquitous |
| 2 | Chinese Money Plant | *Pilea peperomioides* | **also the pothos↔Pilea boundary target — 2-for-1** |
| 3 | Chinese Evergreen | *Aglaonema* | beginner staple |
| 4 | Elephant Ear | *Alocasia spp.* | hot collector plant |
| 5 | Anthurium | *Anthurium andraeanum* | top flowering houseplant |
| 6 | Dumb Cane | *Dieffenbachia spp.* | common foliage |
| 7 | Prayer Plant | *Maranta leuconeura* | pairs with existing Calathea |
| 8 | Boston Fern | *Nephrolepis exaltata* | mass-market classic |
| 9 | Money Tree | *Pachira aquatica* | popular gift plant |
| 10 | Areca Palm | *Dypsis lutescens* | most common indoor palm |
| 11 | Dracaena (generic) | *Dracaena* (marginata/fragrans) | distinct from KB snake plant |
| 12 | Tradescantia | *Tradescantia* | popular trailing plant |
| 13 | English Ivy | *Hedera helix* | common; toxicity-relevant |
| 14 | Schefflera | *Schefflera* (umbrella) | common floor plant |
| 15 | Kalanchoe | *Kalanchoe* | flowering succulent |
| 16 | **Poinsettia** | *Euphorbia pulcherrima* | **user owns one** (seasonal but in-scope per user) |
| 17 | **Venus Flytrap** | *Dionaea muscipula* | **user owns one** (novelty but in-scope per user) |

This would roughly **double real KB coverage (10 → ~27 species) with zero ML work**. Popularity ranking
is a reasoned judgment, not a hard data source — a planning step could firm it up, but the user has
already approved this set + the two owned additions.

**Caveats for the planner:**
- **Calibration is unprobed for these 37 delta classes** — only a couple have ever been probed. Adding
  KB entries means low-confidence routing / confident-wrong behavior matters more for classes we've
  never measured.
- **Pilea is the sharp one.** Today a pothos→Pilea misread shows *nothing*. Once Pilea has a KB entry,
  that same misclassification would confidently surface the **wrong** care card. So if Pilea is covered
  this sprint, the boundary fix (or strict confidence gating on the pothos/Pilea pair) should ship
  alongside it — don't add the Pilea KB entry naked.
- **KB was LOCKED through 0007/0008.** This sprint deliberately unlocks it for additive entries. Keep
  the existing 16 entries untouched; this is append-only.

### CONSTRAINT — no self-shot / first-party training imagery (hard preference)

The user does **not** want any sprint that requires them (or anyone) to photograph plants for training
data. This directly invalidates two recommendations in `finetune-sprint-outline.md`:

- **`philodendron-pink-princess` self-shot fallback (the *recommended* path) is OFF the table.** With
  cultivar-proven CC imagery at ~5–15 and no cultivar-specific paid/CC dataset, pink-princess has **no
  viable data path** under this constraint → it stays NO-GO and **drops out of fine-tune scope** unless
  a future CC source materializes. Do not plan a self-shot batch for it.
- **The pothos/Pilea boundary supplement (~75–150 self-shot Pilea hard examples) is OFF the table.** The
  Pilea side (~76 CC usable) must rely on the CC images + augmentation only, or the boundary pass gets
  descoped. Note this dovetails with the KB-expansion direction: the higher-leverage near-term move for
  Pilea is a KB entry + confidence gating, not a balanced retrain.

### Sequencing reconsideration

The user asked to **reconsider the 0008 hand-off sequencing.** Combined with the two items above, the
recommended re-sequencing is:

1. **Next sprint = KB-expansion over the model-covered delta** (text-only, ~17 species, no ML, no
   self-shot) — the cheap coverage win the user approved.
2. **Fine-tune sprint = deferred / descoped.** When/if revisited: 5 GO OOV classes
   (chlorophytum, hederaceum, hoya, adansonii, ficus-lyrata) remain viable on CC data alone;
   pink-princess and the Pilea-balance work are **out** under the no-self-shot constraint. The
   `finetune-sprint-outline.md` shared approach (47→52, float16 export, `ModelSwapEvaluationTest`,
   `ACTIVE_MODEL_ROOT` reuse) still stands for the 5 GO classes.

## Notes for Next Sprint

- The 0008 evidence artifacts (`data-availability-report.md`, `go-no-go-matrix.md`,
  `finetune-sprint-outline.md`, `source-counts.csv`, `query-log.md`) are sound and remain the reference
  for any *future* fine-tune; nothing in them needs to be redone. The redirection is about *which* path
  to fund first, not about correcting the spike.
- KB-expansion sprint is **additive-only** to the locked KB; existing 16 species and the frozen
  `PlantIdentifier` / `IdentificationResult` seam stay untouched. New `plant_class_map.json` rows map
  model classes → new KB ids; `verifyNoNetworking` + `check-stub-isolation.sh` must stay GREEN.
- Re-confirm exact botanical/care content per new species during planning (the table above is the
  target list, not vetted care data).
</content>
</invoke>
