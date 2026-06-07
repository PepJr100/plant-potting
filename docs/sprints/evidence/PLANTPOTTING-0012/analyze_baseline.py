#!/usr/bin/env python3
"""PLANTPOTTING-0012 Phase 2 — analyse the pre-Pilea baseline eval CSV.

Produces baseline-pre-pilea-summary.md: the pothos↔Pilea boundary scorecard, the live
pothos→Pilea failure quantification, and the naive-Pilea-mapped simulation across the three
acceptance surfaces (all clean, all perturbations, pothos/Pilea subset).
"""
import csv, os, sys
try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

HERE = os.path.dirname(__file__)
CSV = os.path.join(HERE, "baseline-pre-pilea-eval.csv")
PILEA_LABEL = "Chinese Money Plant (Pilea peperomioides)"
PILEA_ID = "pilea-peperomioides"
POTHOS_ID = "epipremnum-aureum"

# shipped thresholds (model_manifest.json)
PLAIN, MARGIN_MIN, MARGIN_DELTA, ABSTAIN = 0.55, 0.45, 0.18, 0.30

rows = list(csv.DictReader(open(CSV, encoding="utf-8")))
for r in rows:
    r["raw_top1_score"] = float(r["raw_top1_score"])
    r["raw_top2_score"] = float(r["raw_top2_score"])
    r["margin"] = float(r["margin"])

def naive_pilea_highconf(r):
    """Would a NAIVE (gateless) Pilea mapping make this row a high-confidence Pilea card?"""
    if r["raw_top1_label"] != PILEA_LABEL:
        return False
    s, m = r["raw_top1_score"], r["margin"]
    high = (s >= PLAIN) or (s >= MARGIN_MIN and m >= MARGIN_DELTA)
    return high and (m >= ABSTAIN)

def naive_confident_wrong(r):
    """confident_wrong under naive gateless Pilea mapping."""
    if naive_pilea_highconf(r):
        return r["expected_species_id"] != PILEA_ID          # Pilea card vs truth
    return r["confident_wrong"] == "true"                     # unchanged elsewhere

MODES = ["squash", "center_crop", "tta6"]
out = []
out.append("# PLANTPOTTING-0012 — baseline (pre-Pilea) boundary scorecard\n")
out.append("Source: `baseline-pre-pilea-eval.csv` (production model, **Pilea in KB but UNMAPPED, "
           "gate absent**). Production pipeline = `tta6` (squash + 6-crop TTA). Perturbation rows "
           "are synthetic-robustness, reported separately from clean base photos.\n")

# ---- pothos→Pilea failure quantification (per mode, CLEAN base photos) ----
out.append("## 1. Live pothos→Pilea failure (clean base photos)\n")
out.append("| mode | pothos fixtures | top-1 = Pilea | top-1 = Pilea @≥0.55 | mean Pilea-when-top1 score |\n")
out.append("|---|---|---|---|---|\n")
for mode in MODES:
    pothos = [r for r in rows if r["expected_species_id"] == POTHOS_ID
              and r["mode"] == mode and r["perturbation"] == "clean"]
    t1pilea = [r for r in pothos if r["raw_top1_label"] == PILEA_LABEL]
    hi = [r for r in t1pilea if r["raw_top1_score"] >= PLAIN]
    mean = sum(r["raw_top1_score"] for r in t1pilea) / len(t1pilea) if t1pilea else 0.0
    out.append(f"| {mode} | {len(pothos)} | {len(t1pilea)} | {len(hi)} | {mean:.4f} |\n")

# ---- per-fixture detail (clean, tta6 = production) ----
out.append("\n## 2. Per-fixture boundary detail (clean, `tta6` = production)\n")
out.append("Route reason: a pothos that raw-predicts Pilea routes to the picker TODAY only because "
           "its top-1 (Pilea) is unmapped — that silent miss is what naive mapping would turn "
           "confidently wrong.\n\n")
out.append("| fixture | expected | raw top-1 | score | margin | mapped | route | conf-wrong (now) | naive-mapped → |\n")
out.append("|---|---|---|---|---|---|---|---|---|\n")
subset = [r for r in rows if r["expected_species_id"] in (POTHOS_ID, PILEA_ID)
          and r["mode"] == "tta6" and r["perturbation"] == "clean"]
for r in sorted(subset, key=lambda r: (r["expected_species_id"], r["base_image"])):
    top1short = "Pilea" if r["raw_top1_label"] == PILEA_LABEL else r["raw_top1_label"][:22]
    naive = "Pilea card (WRONG)" if (naive_pilea_highconf(r) and r["expected_species_id"] != PILEA_ID) \
        else ("Pilea card (correct)" if naive_pilea_highconf(r) else "picker")
    out.append(f"| {r['base_image']} | {r['expected_species_id']} | {top1short} | "
               f"{r['raw_top1_score']:.3f} | {r['margin']:.3f} | {r['mapped_top1_kb_id'] or '—'} | "
               f"{r['route']} | {r['confident_wrong']} | {naive} |\n")

# ---- naive-Pilea-mapped simulation across the 3 surfaces × modes ----
out.append("\n## 3. Naive-Pilea-mapped simulation (gateless) — confident-wrong delta\n")
out.append("\"If we mapped Pilea with **no gate**, how many results turn confidently wrong?\" "
           "Baseline = Pilea unmapped (today). Δ = new confident-wrong introduced purely by the "
           "naive mapping. This is the number the Phase 6 gated result must NOT exceed (it must be 0 added).\n\n")
def surface_rows(mode, surface):
    rr = [r for r in rows if r["mode"] == mode]
    if surface == "clean":
        return [r for r in rr if r["perturbation"] == "clean"]
    if surface == "perturb":
        return [r for r in rr if r["perturbation"] != "clean"]
    if surface == "boundary":
        return [r for r in rr if r["expected_species_id"] in (POTHOS_ID, PILEA_ID)]
    return rr
for mode in MODES:
    out.append(f"### mode: {mode}\n")
    out.append("| surface | n | baseline conf-wrong | naive conf-wrong | Δ (added by naive Pilea) |\n")
    out.append("|---|---|---|---|---|\n")
    for surface, label in [("clean", "all clean"), ("perturb", "all perturbations"),
                           ("boundary", "pothos/Pilea subset (all inputs)")]:
        rr = surface_rows(mode, surface)
        if not rr:
            continue
        base_cw = sum(1 for r in rr if r["confident_wrong"] == "true")
        naive_cw = sum(1 for r in rr if naive_confident_wrong(r))
        n = len(rr)
        out.append(f"| {label} | {n} | {base_cw} ({base_cw/n:.3f}) | {naive_cw} ({naive_cw/n:.3f}) | "
                   f"+{naive_cw - base_cw} |\n")
    out.append("\n")

# ---- latency (production tta6) ----
lat = sorted(int(r["latency_ms"]) for r in rows if r["mode"] == "tta6" and int(r["latency_ms"]) >= 0)
out.append("## 4. Latency (`tta6`, all rows)\n")
out.append(f"median {lat[len(lat)//2]} ms · worst {lat[-1]} ms · n={len(lat)}\n")

open(os.path.join(HERE, "baseline-pre-pilea-summary.md"), "w", encoding="utf-8").write("".join(out))
print("".join(out))
