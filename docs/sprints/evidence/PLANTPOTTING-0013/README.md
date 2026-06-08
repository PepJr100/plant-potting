# PLANTPOTTING-0013 — evidence dir

Direct Pilea care card (conditional) + honest CC0/PD sample-count growth + doc cleanup (v0.7.0).

## Baseline fixture counts (start of sprint)

Source: `app/src/androidTest/assets/identify-fixtures/fixture-manifest.tsv` (state at 0012 close, 60 fixtures).

- **Pilea (`pilea-peperomioides`): 6 fixtures** (`__01..06`) — authors: dinomariobob, Tiago Lubiana,
  Olsza Borys, Daniel Atha, dmagdee, Curran Dwyer (all distinct → author-separation is trivial).
  **Pilea is deliberately NOT deepened this sprint** (non-goal — we do not source Pilea photos to
  flatter its own gate).
- **Pothos (`epipremnum-aureum`): 7 fixtures** (base + `__01..06`).
- **8 single-photo mapped species (Thread B targets, each exactly one `__01.jpg`):**
  `saintpaulia-ionantha, chamaedorea-elegans, beaucarnea-recurvata, alocasia, dracaena, begonia,
  ctenanthe, schlumbergera-bridgesii`.

## Constraints (from the plan non-goals)

- Frozen seam: `PlantIdentifier` / `IdentificationResult` / `IdSource` untouched.
- No model swap / retrain / re-quantize; AIY V1/3 anchor untouched. `tta` stays **6**.
- No new Pilea fixtures; no self-shot / first-party / generated imagery; new fixtures **CC0 / PD only**.
- No loosening of the global plain/margin gates or the 0011 abstain margin (0.30). The elevated Pilea
  bar is an **additional** gate, never a relaxation. `T_pilea` must be CI-bound **> 0.9661**.

## Strict-picker baseline (the +0-confident-wrong reference)

`baseline-strict-picker-eval.csv` — the production strict-picker eval. **The production gate config is
byte-identical to the 0012-shipped state** (`per_species_thresholds = {}`, `boundary_pairs` = top-1
Pilea → picker, `tta = 6`, plain 0.55, abstain 0.30), and **no model / preprocessing / fixture change
has occurred since 0012**, so the committed 0012 `post-pilea-gated-eval.csv` *is* the strict-picker
baseline — re-running `AccuracyEvalTest` on `pixel6Api34` against this exact config reproduces it. We
carry it forward verbatim as the reference rather than burning a redundant identical device cycle. The
**expanded** scorecard (after Thread B adds fixtures) is re-run fresh on device (`expanded-scorecard.csv`).

This is sound for the Thread A comparison because the gate refinement changes routing **only** for rows
whose raw top-1 resolves to Pilea (the 6 Pilea fixtures + the pothos→Pilea misreads). Thread B adds
**no** Pilea fixtures and touches none of those rows, so the strict-picker-vs-direct-card
confident-wrong delta is independent of the Thread B fixture growth.

## Planned evidence files

- `baseline-strict-picker-eval.csv` — ✅ the +0-confident-wrong reference (carried from 0012; see above).
- `pilea-direct-card-eval-plan.md` — pre-registered A1 protocol (candidate rule, pre-committed grid,
  LOO + author-separation method, pass/fail bar, fall-back rule). Written **before** any production change.
- `pilea-direct-card-threshold-sweep.csv` — the offline-simulator fold/threshold table.
- `simulate_pilea_threshold.py` — the offline threshold simulator (reads the baseline CSV; no app code).
- `pilea-direct-card-decision.md` — the two distributions, separation gap, LOO fold table, author
  partition, chosen `T_pilea` (or blocker rationale), and the SHIP / FALL-BACK decision.
- `post-direct-card-eval.csv` + `direct-card-gating-decision.md` — only if Thread A ships the direct card.
- `fixture-sourcing-log.md` — Thread B search/accept/reject log + the 8-species reason-coded coverage table.
- `expanded-scorecard.csv` + summary — the refreshed honest headline number over the expanded fixture set.
