#!/usr/bin/env python3
"""PLANTPOTTING-0013 — analyze the on-device expanded scorecard (direct-card config, T_pilea=0.98).

This ONE device run (manifest per_species_thresholds[pilea]=0.98 active, expanded 66-fixture set) serves
both Thread A's post-direct-card eval and Thread B's expanded scorecard. Compares against the
strict-picker baseline for the +0-confident-wrong bar, validates the Pilea/pothos routing invariants,
and prints the refreshed honest headline number with per-species counts.

Usage: python3 analyze_scorecard.py <expanded-scorecard.csv> [baseline-strict-picker-eval.csv]
"""
import csv, sys, os
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
POST = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "expanded-scorecard.csv")
BASE = sys.argv[2] if len(sys.argv) > 2 else os.path.join(HERE, "baseline-strict-picker-eval.csv")
PILEA, POTHOS = "pilea-peperomioides", "epipremnum-aureum"


def load(p):
    with open(p, newline="", encoding="utf-8") as f:
        return list(csv.DictReader(f))


def cw_counts(rows, mode):
    """confident-wrong counts on clean / all-perturbation / pothos-Pilea-subset for a mode."""
    m = [r for r in rows if r["mode"] == mode and r["failure"] == ""]
    clean = [r for r in m if r["perturbation"] == "clean"]
    pp = [r for r in m if r["expected_species_id"] in (PILEA, POTHOS)]
    cw = lambda rs: sum(1 for r in rs if r["confident_wrong"].lower() == "true")
    return {"clean": (cw(clean), len(clean)), "allpert": (cw(m), len(m)), "pothosPilea": (cw(pp), len(pp))}


def main():
    post = load(POST)
    base = load(BASE) if os.path.exists(BASE) else None
    print(f"post rows={len(post)}  base rows={len(base) if base else 'n/a'}\n")

    # ---- +0 confident-wrong bar vs baseline, per mode (GATE-ISOLATED) ----
    # The gate refinement only changes routing for Pilea-top-1 rows; Thread B added 6 NEW non-Pilea
    # fixtures. To isolate the gate effect from the fixture-set growth, restrict `post` to the SAME
    # base images as the baseline before comparing. (The pothos/Pilea subset is identical in both sets
    # regardless, so its delta is always gate-only.)
    print("=== confident-wrong: gate-isolated (post restricted to the 60 baseline fixtures) vs baseline ===")
    print("    binding bar: Δ must be +0 on the SHIPPED (tta6) pipeline")
    base_imgs = {r["base_image"] for r in base} if base else set()
    postR = [r for r in post if r["base_image"] in base_imgs] if base else post
    for mode in ("squash", "center_crop", "tta6"):
        shipped = "  <-- SHIPPED" if mode == "tta6" else ""
        pc = cw_counts(postR, mode)
        line = f"mode={mode}{shipped}"
        for surf in ("clean", "allpert", "pothosPilea"):
            pcw, pn = pc[surf]
            if base:
                bc = cw_counts(base, mode)[surf]
                line += f"  {surf}={pcw}/{pn}(Δ{pcw - bc[0]:+d})"
            else:
                line += f"  {surf}={pcw}/{pn}"
        print(line)
    if base:
        print(f"  (isolation check: post-restricted fixtures={len({r['base_image'] for r in postR})}, "
              f"baseline fixtures={len(base_imgs)} — must match)")

    # ---- invariant 1: no pothos fixture surfaces a direct Pilea card (tta6 clean) ----
    print("\n=== invariant: pothos fixtures never → direct Pilea card (tta6 clean) ===")
    viol = 0
    for r in post:
        if r["mode"] != "tta6" or r["perturbation"] != "clean":
            continue
        if r["expected_species_id"] != POTHOS:
            continue
        direct_pilea = r["route"] == "high-conf" and r["mapped_top1_kb_id"] == PILEA
        if direct_pilea:
            viol += 1
        print(f"  {r['base_image']:28s} raw={r['raw_top1_score']:>6} route={r['route']:9s} "
              f"mapped1={r['mapped_top1_kb_id']:20s} direct-Pilea={'YES!!' if direct_pilea else 'no'}")
    print(f"  pothos→direct-Pilea violations: {viol}  (must be 0)")

    # ---- invariant 2: real-Pilea routing (tta6 clean): above T_pilea→direct, else picker Pilea-first ----
    print("\n=== Pilea fixtures routing (tta6 clean), T_pilea=0.98 ===")
    for r in sorted([r for r in post if r["mode"] == "tta6" and r["perturbation"] == "clean"
                     and r["expected_species_id"] == PILEA], key=lambda r: r["base_image"]):
        top3 = r["mapped_top3_kb_ids"]
        pilea_first = top3.split("|")[0] == PILEA if top3 else False
        print(f"  {r['base_image']:28s} raw={r['raw_top1_score']:>6} route={r['route']:9s} "
              f"mapped1={r['mapped_top1_kb_id']:20s} pilea_first_in_top3={pilea_first}")

    # ---- headline: per-base-image clean top-1 + confident-wrong (tta6), per-species counts ----
    print("\n=== headline (tta6 shipped pipeline) ===")
    tta6 = [r for r in post if r["mode"] == "tta6" and r["failure"] == ""]
    clean = [r for r in tta6 if r["perturbation"] == "clean"]
    by_base = defaultdict(list)
    for r in clean:
        by_base[r["base_image"]].append(r)
    correct = sum(1 for b, rs in by_base.items()
                  if any(x["route"] == "high-conf" and x["mapped_top1_kb_id"] == x["expected_species_id"] for x in rs))
    print(f"  per-base-image clean top-1 (high-conf correct): {correct}/{len(by_base)} = {correct/len(by_base):.3f}")
    cwc = sum(1 for r in clean if r["confident_wrong"].lower() == "true")
    print(f"  clean confident-wrong: {cwc}/{len(clean)} = {cwc/len(clean):.3f}")
    pert = [r for r in tta6 if r["perturbation"] != "clean"]
    cwp = sum(1 for r in pert if r["confident_wrong"].lower() == "true")
    print(f"  perturbation confident-wrong: {cwp}/{len(pert)} = {cwp/len(pert):.3f}")

    counts = defaultdict(int)
    for r in clean:
        counts[r["expected_species_id"]] += 1
    print(f"\n  per-species clean fixture counts (n={len(counts)} species, {len(clean)} fixtures):")
    print("   " + ", ".join(f"{k}={v}" for k, v in sorted(counts.items())))


if __name__ == "__main__":
    main()
