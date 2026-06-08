#!/usr/bin/env python3
"""PLANTPOTTING-0013 Phase A1 — offline T_pilea threshold simulator.

Decouples direct-card threshold iteration from the slow, wipe-on-reinstall GMD device cycle by
simulating the gate refinement purely from the committed AccuracyEvalTest CSV. Raw model scores are
model-intrinsic (the gate refinement changes *routing*, not scores), so the strict-picker baseline CSV
already contains every raw Pilea top-1 score the simulation needs.

The gate refinement (what this simulates): a result whose raw top-1 resolves to `pilea-peperomioides`
routes to the LowConfidencePicker UNLESS its top-1 score >= T_pilea, in which case it yields a direct
Pilea card. A pothos misread as Pilea that crosses T_pilea would therefore become a confidently-wrong
direct Pilea card — that is the event the bar forbids.

Usage:  python3 simulate_pilea_threshold.py
Reads:  baseline-strict-picker-eval.csv (same dir)
Writes: pilea-direct-card-threshold-sweep.csv (same dir)
Prints: distributions, separation gap (per mode + shipped tta6), and the LOO fold table.
"""
import csv
import os

HERE = os.path.dirname(os.path.abspath(__file__))
CSV_IN = os.path.join(HERE, "baseline-strict-picker-eval.csv")
CSV_OUT = os.path.join(HERE, "pilea-direct-card-threshold-sweep.csv")

PILEA_LABEL_FRAGMENT = "Pilea peperomioides"  # raw_top1_label of a top-1=Pilea result
POTHOS_ID = "epipremnum-aureum"
PILEA_ID = "pilea-peperomioides"
SHIPPED_MODE = "tta6"  # production = squash preprocessing + 6-crop TTA (model_manifest.json tta=6)
CI_FLOOR = 0.9661     # hard CI floor: T_pilea must be > this (documented clean-squash pothos ceiling)

# Pre-committed grid (registered in pilea-direct-card-eval-plan.md before any fold was inspected).
GRID = [0.9700, 0.9750, 0.9800, 0.9850, 0.9900, 0.9950, 0.9990, 0.9998]

# Author partition (fixture-manifest.tsv). All 6 distinct -> every LOO fold is author-separated.
PILEA_AUTHORS = {
    "pilea-peperomioides__01": "dinomariobob",
    "pilea-peperomioides__02": "Tiago Lubiana",
    "pilea-peperomioides__03": "Olsza Borys",
    "pilea-peperomioides__04": "Daniel Atha",
    "pilea-peperomioides__05": "dmagdee",
    "pilea-peperomioides__06": "Curran Dwyer",
}


def load_rows():
    with open(CSV_IN, newline="", encoding="utf-8") as f:
        return list(csv.DictReader(f))


def pothos_to_pilea_events(rows, mode):
    """(base_image, perturbation, score) for pothos fixtures whose raw top-1 resolves to Pilea."""
    out = []
    for r in rows:
        if r["mode"] != mode:
            continue
        if r["expected_species_id"] != POTHOS_ID:
            continue
        if PILEA_LABEL_FRAGMENT in r["raw_top1_label"]:
            out.append((r["base_image"], r["perturbation"], float(r["raw_top1_score"])))
    return out


def real_pilea_clean(rows, mode):
    """{base_image: clean top-1 score} for Pilea fixtures (clean perturbation, top-1=Pilea)."""
    out = {}
    for r in rows:
        if r["mode"] != mode or r["perturbation"] != "clean":
            continue
        if r["expected_species_id"] != PILEA_ID:
            continue
        if PILEA_LABEL_FRAGMENT in r["raw_top1_label"]:
            out[r["base_image"]] = float(r["raw_top1_score"])
    return out


def main():
    rows = load_rows()

    print("=" * 78)
    print("DISTRIBUTIONS (per mode)")
    print("=" * 78)
    for mode in ("squash", "center_crop", "tta6"):
        pothos = pothos_to_pilea_events(rows, mode)
        pilea = real_pilea_clean(rows, mode)
        pmax = max((s for _, _, s in pothos), default=0.0)
        pfloor = min(pilea.values()) if pilea else 0.0
        gap = pfloor - pmax
        shipped = "  <-- SHIPPED PIPELINE" if mode == SHIPPED_MODE else ""
        print(f"\nmode={mode}{shipped}")
        print(f"  pothos->Pilea events: {len(pothos)}  max(ceiling)={pmax:.4f}")
        for bi, pert, s in sorted(pothos, key=lambda t: -t[2]):
            print(f"      {s:.4f}  {bi}  [{pert}]")
        print(f"  real-Pilea clean floor={pfloor:.4f}  (per fixture: "
              + ", ".join(f"{k.split('__')[-1]}={v:.4f}" for k, v in sorted(pilea.items())) + ")")
        print(f"  SEPARATION GAP (floor - ceiling) = {gap:+.4f}  -> {'POSITIVE' if gap > 0 else 'NON-POSITIVE'}")

    # ---- LOO + author separation on the shipped (tta6) pipeline ----
    mode = SHIPPED_MODE
    pothos = pothos_to_pilea_events(rows, mode)
    pothos_ceiling = max((s for _, _, s in pothos), default=0.0)
    pilea = real_pilea_clean(rows, mode)
    fixtures = sorted(pilea.keys())

    def select_T(tuning_scores):
        """Smallest grid T with T>CI_FLOOR and every tuning fixture clearing it."""
        tmin = min(tuning_scores)
        for t in GRID:
            if t > CI_FLOOR and t <= tmin + 1e-9:
                return t
        return None

    print("\n" + "=" * 78)
    print(f"LEAVE-ONE-OUT + AUTHOR SEPARATION (shipped pipeline: {mode})")
    print(f"pothos->Pilea ceiling ({mode}, all perturbations) = {pothos_ceiling:.4f}")
    print("=" * 78)

    sweep = []
    all_pass = True
    for held in fixtures:
        tuning = [f for f in fixtures if f != held]
        tuning_scores = [pilea[f] for f in tuning]
        held_author = PILEA_AUTHORS[held]
        tuning_authors = [PILEA_AUTHORS[f] for f in tuning]
        author_sep = held_author not in tuning_authors
        T = select_T(tuning_scores)
        # safety: does ANY pothos->Pilea event cross T on the shipped pipeline?
        crossing = [(bi, pert, s) for bi, pert, s in pothos if s >= T] if T else []
        cw = len(crossing)
        held_clears = pilea[held] >= T if T else False
        fold_pass = (cw == 0) and author_sep
        all_pass = all_pass and fold_pass
        sentinel = "none cross" if cw == 0 else ";".join(f"{bi}[{pert}]={s:.4f}" for bi, pert, s in crossing)
        sweep.append({
            "fold_held_out": held,
            "held_out_author": held_author,
            "tuning_authors": "|".join(tuning_authors),
            "author_separated": author_sep,
            "selected_T_pilea": f"{T:.4f}" if T else "none",
            "held_out_top1_score": f"{pilea[held]:.4f}",
            "held_out_route": "direct-card" if held_clears else "picker",
            "held_out_correct": held_clears,  # held-out fixture IS a real Pilea, so direct card == correct
            "confident_wrong_count": cw,
            "pothos_sentinel": sentinel,
            "fold_pass": fold_pass,
        })
        print(f"\nfold: hold out {held} (author={held_author})")
        print(f"  author-separated from tuning set: {author_sep}")
        print(f"  selected T_pilea={T:.4f}  held-out score={pilea[held]:.4f}"
              f"  -> {'direct-card (benefit restored)' if held_clears else 'picker'}")
        print(f"  pothos->Pilea crossing T: {cw}  ({sentinel})")
        print(f"  FOLD {'PASS' if fold_pass else 'FAIL'} (+{cw} confident-wrong)")

    with open(CSV_OUT, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=list(sweep[0].keys()))
        w.writeheader()
        w.writerows(sweep)

    print("\n" + "=" * 78)
    print(f"LOO VERDICT: {'ALL FOLDS PASS (+0 confident-wrong on shipped pipeline)' if all_pass else 'FAIL'}")
    print(f"Wrote {CSV_OUT}")
    print("=" * 78)


if __name__ == "__main__":
    main()
