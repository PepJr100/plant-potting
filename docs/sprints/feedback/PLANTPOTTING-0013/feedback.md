# PLANTPOTTING-0013 Feedback

Review date: 2026-06-08. Reviewer: principal (whichrobevans). Sprint shipped clean — all
110 plan checkboxes flipped, exec merged via PR #41 (`50fa09a`), v0.7.0 released. All automated
gates re-run green during review (unit tests, verifyNoNetworking, ktlintCheck, lintDebug,
stub-isolation). Frozen seam / model / AIY anchor / tta=6 / class-map=39 / `T_pilea > 0.9661`
CI-bind all re-confirmed. APK + tag + public Release all verified present.

**On-device confirmation:** the principal photographed a real Pilea on the v0.7.0 build — the
**direct Pilea care card surfaced correctly, as expected.** Thread A's headline behaviour is
confirmed in the wild.

No defects in the sprint's *committed* scope. The items below are a UX bug discovered during
review plus forward-looking direction for the next sprint(s).

## Bugs

### Pilea direct-card (and picker) shows no hero image — placeholder only
**Reproduction:** Photograph a real Pilea on v0.7.0 → direct Pilea care card surfaces → the card's
reference image is the generic placeholder vector, not a Pilea photo.
**Expected:** A CC0/PD Pilea hero photo, like every other mapped species' card.
**Actual:** `ic_plant_placeholder` fallback (no picture).
**Scope — "what other species is this true for?":** Investigated. Among the **39 mapped species
(the ones that can surface a card), `pilea-peperomioides` is the ONLY one with no hero image.**
Every other mapped species has a CC0/PD `.webp` in `res/drawable-nodpi/`. (Verified by diffing
`plant_class_map.json` mapped kbSpeciesIds against the keys in `PlantImageResolver.kt`.)
**Root cause:** `PlantImageResolver.kt` maps speciesId → drawable; Pilea isn't in the map. Pilea
was mapped *strict-picker* in 0012 and only became *direct-card-eligible* in 0013, but its hero
image was never added. It has actually been showing the placeholder in the **LowConfidencePicker
since 0012** too — only now visible because the direct card surfaces.
**Why no test caught it:** `ReferenceImageManifestTest` only asserts every *bundled image* has a
license manifest entry (image → manifest). It does **not** assert the reverse (card-reachable
species → non-placeholder image). So nothing went red.
**Fix direction:** Use the `/reference-photos` skill to source a CC0/PD Pilea photo; add it to
`PlantImageResolver.kt` + the reference-image manifest + `docs/licenses/`. Add a test binding
*direct-card-eligible (and ideally all mapped) species → a non-placeholder drawable* so this class
of gap can't regress when a species is newly promoted to a card.
**Impact:** Medium — cosmetic but on the sprint's headline feature (the Pilea card). Small,
self-contained fix. Good candidate for the next sprint or a quick fix sprint.

## UX Issues

(none beyond the missing-image bug above)

## Missing Features / Ideas for Next Sprint

### Cascade routing: primary model low-conf → old AIY model (with TTA6) — SPIKE candidate
**Idea (principal):** The old AIY MobileNet still ships (pinned as the V1/3 regression anchor,
`OnDeviceModelRealInterpreterTest`). When the primary `house_plant_species_mobilenetv2` abstains /
low-confidences, route the image through AIY (applying TTA6) and surface any high-confidence AIY
hit. Principal explicitly requested feedback on this.
**Feedback / analysis:**
- *Mechanically feasible.* AIY is already on-device and runs through the same preprocessing
  harness, so TTA6 on AIY is trivial. It would wrap/touch the frozen `PlantIdentifier` seam → a
  real architectural change, not a tweak.
- *Weak prior — tiny vocab overlap.* AIY is a general natural-world classifier; only ~5 of our 39
  mapped species are in AIY's vocabulary. For most low-confidence houseplant cases AIY has **no
  relevant class to offer** and cannot recover the species.
- *Cardinal-sin risk.* "AIY high-confidence" ≠ "AIY correct." 0011's whole point was abstention to
  kill confident-wrong; routing abstentions into a second, *uncalibrated-on-our-fixtures* model
  risks **reintroducing confident-wrong**. Any cascade must clear the same `+0 confident-wrong`
  held-out bar Pilea was held to.
- *Cheapest first step:* offline-measure "recoverable abstentions" on the existing 66-fixture eval
  — of the cases the primary abstains, how many does AIY (a) have in vocab at all and (b) get right
  with high confidence? If ≈0 (likely), the idea is cheaply disproven.
- *Better framing — agreement-as-confidence:* rather than AIY *overriding*, use model **agreement**
  as a trust signal (both models independently agree on a species → strong confirm; disagree →
  keep abstaining). Raises trust without adding confident-wrong risk; fits the calibration
  philosophy better than "surface AIY's hit."
**Disposition:** Worth a measurement spike (offline recoverable-abstention count first, behind the
+0-cw bar). Good `/sprint-planner` candidate.

### Lower / re-derive `T_pilea` to admit more real Pilea
**Request (principal):** 0.98 feels too strict; would like to admit more genuine Pilea. (Initial
ask was "drop to 0.9" — flagged in review that 0.9 is unsafe: it's below the shipped tta6
pothos→Pilea ceiling of 0.9063 **and** below the `> 0.9661` CI bind, so it would readmit
confident-wrong pothos→Pilea cards and turn CI red.)
**Goal clarified:** be less conservative *safely*, not override the safety bar.
**Direction:** There's headroom within the current CI bind — the `> 0.9661` floor is set at the
*single-crop diagnostic* pothos ceiling, while the **shipped tta6** ceiling is only 0.9063. A value
around ~0.97 would admit more Pilea and still pass CI. Next sprint should re-derive `T_pilea`
empirically with **more real Pilea photos** (see fixture-depth ask below), finding the lowest value
that clears the pothos ceiling with margin.

### Harmonise the per-species "bespoke rules" into a single policy — DESIGN discussion
**Observation (principal):** Pilea is currently the **only** species with bespoke rules — the
`boundary_pairs` entry (0012) and the `per_species_thresholds` entry (0013). Every other species
runs on the global rules (plain 0.55 + abstain margin 0.30). Principal wants to discuss harmonising
these into a single set.
**Feedback:** The Pilea rules exist *because* the pothos→Pilea confusion is genuinely
species-specific and **asymmetric** (pothos confidently misreads as Pilea, not vice-versa) — a flat
uniform policy can't express "this one pair confuses in one direction." So "harmonise" likely means
one of: (a) a **general per-species/per-pair mechanism** any species can opt into (keeps the
protection, removes the one-off feel) — recommended; or (b) collapse back to global rules (simpler,
loses the pothos→Pilea guard). A `/sprint-planner` design conversation.

### Deeper fixture set — 3+ CC0/PD photos per species
**Request (principal):** Wants a deeper picture set — **3+ photos per species** — not just the
current breadth (many species at 1–2). 31 of 39 species still rest on 1–2 photos, so the honest
headline isn't trustworthy *per-species*.
**Direction:** Next sprint, target ≥3 CC0/PD fixtures per species. Pairs naturally with re-deriving
`T_pilea` (more real Pilea) and with the supply-ceiling follow-up below. Maintain the strict
CC0/PD + species-correctness discipline; report supply ceilings honestly.

### Supply-ceiling follow-up: Begonia + Schlumbergera
**Context:** Thread B left `begonia` and `schlumbergera-bridgesii` at 1 fixture each — documented
license-clean supply ceilings (CC0 Begonia are all wild field species, not houseplant rex/wax;
CC0 Schlumbergera are all *S. truncata*, a distinct species from the KB's `bridgesii`).
**Direction:** Widen sources beyond iNaturalist (Wikimedia Commons, GBIF, Pexels/Unsplash CC0). For
Schlumbergera specifically, consider whether the `schlumbergera-bridgesii` KB mapping should be
revisited given `bridgesii` is widely treated as a synonym of/resolving to `truncata`.

## Notes for Next Sprint

- **Headline read accepted as honest.** Per-base clean top-1 0.417→0.530 is correctly attributed
  mainly to the 6 real-Pilea fixtures flipping from picker to correct direct cards (+ 4 of 6 new
  Thread B second-photos landing correct); confident-wrong stayed flat (clean 0.117→0.121, pert
  0.221→0.210). The principal's takeaway: thin per-species coverage is the limiting factor (→ the
  3+/species ask).
- **Residual risk acknowledged & accepted:** the negative separation gap in the *non-shipped*
  single-crop diagnostic modes (squash/center_crop) is documented in
  `pilea-direct-card-decision.md`; shipped path is tta6, where the gap is +0.0877. Any future
  pothos→Pilea fixture that raises the tta6 ceiling toward 0.98 must re-open the decision.
- **Doc cleanup verified correct:** no surviving present-tense "Pilea unmapped / still deferred /
  pileaIsNotMapped passes" claims in `ml-mapping-notes.md`; 0009/0010 prose marked historical, 0013
  section records SHIP `T_pilea=0.98`.
- **Orthogonal backlog:** the monetisation spike in `docs/future-ideas/feature-ideas.md` (incl. the
  "think of 40 ideas" ask) remains an untriaged inbox item — not folded into this sprint.
