#!/usr/bin/env python3
"""PLANTPOTTING-0012 Phase 6 — compare the gated (post-Pilea) eval against the pre-Pilea baseline.

Builds post-pilea-gated-summary.md: baseline / naive-simulated / gated across the three surfaces,
the correct-pothos picker-rate delta, the per-pothos-fixture "never a direct Pilea card" check, and
the true-Pilea visibility check. Reads baseline-pre-pilea-eval.csv + post-pilea-gated-eval.csv.
"""
import csv, os, sys
try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

HERE = os.path.dirname(__file__)
PILEA_LABEL = "Chinese Money Plant (Pilea peperomioides)"
PILEA_ID, POTHOS_ID = "pilea-peperomioides", "epipremnum-aureum"
PLAIN, MARGIN_MIN, MARGIN_DELTA, ABSTAIN = 0.55, 0.45, 0.18, 0.30
MODES = ["squash", "center_crop", "tta6"]

def load(name):
    rows = list(csv.DictReader(open(os.path.join(HERE, name), encoding="utf-8")))
    for r in rows:
        r["raw_top1_score"] = float(r["raw_top1_score"])
        r["margin"] = float(r["margin"])
    return rows

base = load("baseline-pre-pilea-eval.csv")
post = load("post-pilea-gated-eval.csv")

def naive_pilea_highconf(r):
    if r["raw_top1_label"] != PILEA_LABEL:
        return False
    s, m = r["raw_top1_score"], r["margin"]
    return ((s >= PLAIN) or (s >= MARGIN_MIN and m >= MARGIN_DELTA)) and (m >= ABSTAIN)

def naive_cw(r):
    if naive_pilea_highconf(r):
        return r["expected_species_id"] != PILEA_ID
    return r["confident_wrong"] == "true"

def surface(rows, mode, which):
    rr = [r for r in rows if r["mode"] == mode]
    if which == "clean":    return [r for r in rr if r["perturbation"] == "clean"]
    if which == "perturb":  return [r for r in rr if r["perturbation"] != "clean"]
    if which == "boundary": return [r for r in rr if r["expected_species_id"] in (POTHOS_ID, PILEA_ID)]
    return rr

out = ["# PLANTPOTTING-0012 — post-mapping gated eval vs. pre-Pilea baseline (Phase 6)\n",
       "Gated = Pilea mapped + pothos↔Pilea boundary gate ON. The binding bar: gated confident-wrong "
       "must NOT exceed the **baseline** on any surface (i.e. Δ vs baseline ≤ 0), and the naive-"
       "gateless column shows what the gate prevented.\n"]

out.append("## 1. Confident-wrong: baseline vs naive-gateless vs gated\n")
for mode in MODES:
    out.append(f"### mode: {mode}\n")
    out.append("| surface | n | baseline cw | naive cw (gateless) | gated cw | Δ gated−baseline |\n")
    out.append("|---|---|---|---|---|---|\n")
    for which, lab in [("clean","all clean"),("perturb","all perturbations"),("boundary","pothos/Pilea subset")]:
        b = surface(base, mode, which); p = surface(post, mode, which)
        if not b or not p: continue
        bcw = sum(1 for r in b if r["confident_wrong"]=="true")
        ncw = sum(1 for r in b if naive_cw(r))
        pcw = sum(1 for r in p if r["confident_wrong"]=="true")
        n = len(b)
        flag = "" if pcw <= bcw else "  ⚠ REGRESSION"
        out.append(f"| {lab} | {n} | {bcw} ({bcw/n:.3f}) | {ncw} ({ncw/n:.3f}) | {pcw} ({pcw/n:.3f}) | {pcw-bcw:+d}{flag} |\n")
    out.append("\n")

# 2. correct-pothos picker-rate delta (gate must not push correct pothos to the picker)
out.append("## 2. Correct-pothos low-confidence route-rate delta (must be ~0)\n")
out.append("Correct-pothos direct card = pothos fixture routed high-conf to epipremnum-aureum. The gate "
           "fires only on top-1=Pilea, so a correct pothos-dominant result must be untouched.\n\n")
out.append("| mode | pothos rows | base correct-direct | gated correct-direct | base picker | gated picker | Δ picker |\n")
out.append("|---|---|---|---|---|---|---|\n")
for mode in MODES:
    b = [r for r in base if r["mode"]==mode and r["expected_species_id"]==POTHOS_ID]
    p = [r for r in post if r["mode"]==mode and r["expected_species_id"]==POTHOS_ID]
    def correct_direct(rows): return sum(1 for r in rows if r["route"]=="high-conf" and r["mapped_top1_kb_id"]==POTHOS_ID)
    def picker(rows): return sum(1 for r in rows if r["route"]=="low-conf")
    out.append(f"| {mode} | {len(p)} | {correct_direct(b)} | {correct_direct(p)} | {picker(b)} | {picker(p)} | {picker(p)-picker(b):+d} |\n")

# 3. per pothos fixture (tta6 clean): never a direct Pilea card
out.append("\n## 3. No pothos fixture surfaces a direct Pilea card (gated, tta6 clean)\n")
out.append("| fixture | raw top-1 | gated route | gated mapped_top1 | direct Pilea card? |\n|---|---|---|---|---|\n")
viol = 0
for r in sorted([r for r in post if r["mode"]=="tta6" and r["perturbation"]=="clean" and r["expected_species_id"]==POTHOS_ID], key=lambda r: r["base_image"]):
    top1 = "Pilea" if r["raw_top1_label"]==PILEA_LABEL else r["raw_top1_label"][:20]
    badcard = (r["route"]=="high-conf" and r["mapped_top1_kb_id"]==PILEA_ID)
    if badcard: viol += 1
    out.append(f"| {r['base_image']} | {top1} | {r['route']} | {r['mapped_top1_kb_id'] or '—'} | {'YES ⚠' if badcard else 'no'} |\n")
out.append(f"\n**Pothos→direct-Pilea-card violations: {viol}** (must be 0).\n")

# 4. true-Pilea visibility (gated, tta6 clean): picker with Pilea visible in candidates
out.append("\n## 4. True-Pilea fixtures: picker with Pilea visible (gated, tta6 clean)\n")
out.append("| fixture | gated route | mapped_top3 (candidates) | Pilea visible? |\n|---|---|---|---|\n")
missing = 0
for r in sorted([r for r in post if r["mode"]=="tta6" and r["perturbation"]=="clean" and r["expected_species_id"]==PILEA_ID], key=lambda r: r["base_image"]):
    cands = r["mapped_top3_kb_ids"]
    vis = PILEA_ID in cands
    if not vis: missing += 1
    out.append(f"| {r['base_image']} | {r['route']} | {cands} | {'yes' if vis else 'NO ⚠'} |\n")
out.append(f"\n**True-Pilea-without-Pilea-candidate: {missing}** (must be 0).\n")

open(os.path.join(HERE, "post-pilea-gated-summary.md"), "w", encoding="utf-8").write("".join(out))
print("".join(out))
