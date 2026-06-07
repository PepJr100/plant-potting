# PLANTPOTTING-0012 — pothos↔Pilea disambiguation gate: design decision (Phase 3)

**Status: chosen design is provisional, pending the Phase 6 post-mapping evidence.** The *default*
(Candidate B) is justified independently of the TTA sweep by the no-second-place-mass logic below; a
*narrower* rule may replace it only if Phase 6 evidence proves it still blocks every pothos→Pilea
confident card.

## The problem (from the brief + Phase 1 inventory)

The production `house_plant_species_mobilenetv2` confidently confuses pothos with Pilea: a pothos
input yields raw top-1 = `Chinese Money Plant (Pilea peperomioides)` @ **0.9661** — a single-class
confident error with **no second-place mass**. Pilea is safe *today* only because it is unmapped:
pothos's top-1 resolves to nothing and falls through to the `LowConfidencePicker`. Naively mapping
Pilea would convert that silent miss into a *confidently-wrong Pilea care card*. The gate must let
Pilea ship without re-opening that confident-wrong window.

## Candidate evaluation

### Candidate A — pairwise top-k delta rule
When top-1 is Pilea **and** the pothos label is within top-k by a measured delta, route to the picker
with both surfaced.
- **Pro:** precise, minimal, only fires on genuine ambiguity.
- **Con:** the delta has to be tuned on a tiny Pilea set (overfit risk), and — fatally — it does
  *nothing* for the 0.9661 case, where pothos may not appear in top-k at all (no second-place mass).
- **Verdict: rejected as the default** (cannot catch the core error); usable only as a *narrowing*
  refinement on top of B if Phase 6 evidence supports it.

### Candidate B — boundary-pair allow-list (SELECTED, fail-safe default)
When a result's **raw top-1 resolves to `pilea-peperomioides`**, route the pothos↔Pilea pair to the
picker with **both** `pilea-peperomioides` and `epipremnum-aureum` surfaced as candidates.
- **Pro:** robust to the no-second-place-mass case — it keys on top-1 *being* Pilea, so it catches
  the 0.9661 error head-on. Trivially testable. Data-driven (a manifest `boundary_pairs` block), no
  seam change.
- **Pro (the key scoping property):** it fires **only** on top-1 = Pilea. It does **not** fire on
  pothos-dominant (top-1 = pothos) results, so correct, confident, **direct pothos cards are
  preserved**. (Pothos photos that *mispredict* already route to the picker today — no pothos
  regression there.)
- **Con:** a *true* Pilea photo whose top-1 is Pilea also loses its direct card and appears as a
  candidate instead. This is the accepted cost of the fail-safe default (see "User-facing impact").
- **Verdict: selected as the default.**

### Candidate C — Pilea-specific elevated `per_species_thresholds`
Require Pilea to clear a bar far above the global 0.55 before it can map to a card, via the existing
per-species mechanism.
- **Pro:** zero new code paths; keeps pothos direct.
- **Con:** a 0.9661 pothos→Pilea hit blows past almost any plausible bar — **C alone cannot fix the
  core error.** It can only ever *narrow* the window in which a *direct* Pilea card is permitted.
- **Verdict: rejected as the sole fix;** retained as a possible *narrowing* lever combined with B if
  Phase 6 shows a direct Pilea card is safe.

### Candidate D — TTA / preprocessing-only
Hope that ×6/×8/×10/×20 reorders the pothos/Pilea ranking enough to avoid a gate.
- **Pro:** no boundary logic needed if it worked.
- **Con:** almost certainly insufficient for a 0.9661 single-class error — but it must be **measured,
  not assumed** (Phase 7 feeds this back).
- **Verdict: not a substitute for the gate;** measured in Phase 7 as supporting evidence only.

### Why margin-only abstention is explicitly rejected as the sole fix
The 0011 `high_confidence_abstain_margin = 0.30` downgrades a high-confidence verdict whose
top1−top2 margin is too narrow. A 0.9661 top-1 has **no useful second-place mass**, so its margin is
*wide* — the abstain margin never bites. Margin abstention therefore cannot catch this error. The new
gate **composes with** the abstain margin (it is an additional veto), never replaces it.

## Selected design (provisional)

**Candidate B — top-1-resolves-to-Pilea → route to picker, surface {pilea, pothos}** — as the
fail-safe default, justified independently of TTA by the no-second-place-mass logic.

A narrower rule may replace it **only if** Phase 6 evidence shows it still blocks *every* pothos→Pilea
confident card **and** preserves the no-confident-wrong bar:
- **B + measured top-k condition (A):** restrict B to fire only when pothos is also in top-k — risks
  letting the 0.9661 no-second-mass case through, so only acceptable if Phase 6 shows that case never
  occurs on real fixtures (it does occur — so this is unlikely to qualify).
- **B + elevated Pilea threshold (C) to permit a *direct* Pilea card:** allow a direct Pilea card
  only above a high bar. **Direct Pilea cards require author-separated / held-out evaluation before
  being permitted** (the Pilea fixture set is small; ≥3 independent photos earns only the *right to
  evaluate*, not an automatic direct card).

## Implementation surface (no seam change)

- **Config (data):** a `boundary_pairs` block in
  `app/src/main/assets/ml/house_plant_species_mobilenetv2/model_manifest.json`, parsed by the
  existing `ModelManifestReader`. Each entry: `{ top1_kb_species_id, route, surface_kb_species_ids }`.
- **Logic:** `ModelScoreMapper.map()` at the existing score-routing point (it already holds labels,
  mapped KB ids, ranked top-k scores, and thresholds). When the raw top-1 label resolves to a
  `boundary_pairs` trigger species, force the low-confidence route and build a candidate bundle that
  includes every `surface_kb_species_ids` member (each at its own best score), de-duplicated, with
  the true raw-top candidate not buried.
- **Untouched:** `PlantIdentifier`, `IdentificationResult`, `IdSource` (frozen 0003 §4.4 seam).

## User-facing impact

- **Pothos input (the bug):** previously routed to the picker because Pilea was unmapped; *still*
  routes to the picker (now with Pilea **and** pothos visible as candidates) — but can **never**
  surface a direct, confidently-wrong Pilea care card. Net UX: unchanged-or-better (the correct
  pothos is now an explicit candidate).
- **Correct, confident pothos (top-1 = pothos):** **unaffected** — direct pothos card preserved.
- **True Pilea input (top-1 = Pilea):** loses its *direct* card under the default and appears in the
  picker with Pilea as a visible candidate instead. This is the **false-abstain cost** of the
  fail-safe: a correct Pilea is shown one extra tap away rather than confidently. Acceptable because
  the alternative (a direct Pilea card) cannot be permitted until held-out evidence proves a pothos
  input cannot also produce that card.

## Binding design bar (the Phase 6 acceptance gate)

Adding Pilea **must not increase confident-wrong** vs. the Phase 2 pre-Pilea baseline on:
1. all clean fixtures, **and**
2. all perturbations, **and**
3. the pothos/Pilea subset;

**and must not raise the low-confidence route-rate on correct pothos** beyond an agreed, reported
bound. Proposed bound: the correct-pothos picker-rate delta introduced by the gate is **0** under the
default B (B does not fire on top-1 = pothos, so correct pothos cards are untouched by construction);
any non-zero delta observed in Phase 6 is a bug to investigate, not a budget to spend. If confident-
wrong rises anywhere, or correct-pothos abstention rises, tighten the Pilea direct-card rule or fall
back to pair-forced picker — **do not relax the baseline to pass** (0006 discipline).
